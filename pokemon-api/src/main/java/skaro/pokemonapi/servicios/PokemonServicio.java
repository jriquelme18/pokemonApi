package skaro.pokemonapi.servicios;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import skaro.pokeapi.resource.NamedApiResourceList;
import skaro.pokeapi.resource.pokemon.Pokemon;
import skaro.pokeapi.resource.pokemon.PokemonSelect;

@Service
public class PokemonServicio implements InitializingBean {
	@Autowired
	private RestTemplate template = new RestTemplate();
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private List<Pokemon> repositorio;
	
	private Logger logger = LoggerFactory.getLogger(PokemonServicio.class);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		this.repositorio = new ArrayList<>();
	}
	
	public void save(Pokemon pokemon) {
		if ( repositorio.size() == 3 ){
			logger.info("Posee mas de 3 pokemon seleccionados, favor eliminar uno para asignar otro.");
		}else {	
			this.repositorio.add(pokemon);
		}
	}
	
	@Cacheable(cacheNames = "pokemones")
	public Pokemon buscarxid(Integer id) {
		Pokemon p = this.repositorio
				.stream()
				.filter(it -> it.getId() == id)
				.findFirst()
				.orElseThrow(RuntimeException::new);
		logger.info("Pokemones: ", p.toString());
		return p;
	}
	
	public ResponseEntity<Pokemon> findFirstPokemon() {
		return template.getForEntity("https://pokeapi.co/api/v2/pokemon/1/", Pokemon.class);
	}
	//Busqueda de pokemon mediante ID y Nombre
	@Cacheable(cacheNames = "pokemones", key="#idPokemon")
	public ResponseEntity<Pokemon> findPokemonById(String idPokemon, String userId) {
		logger.info("Entro a buscar el pokemon con la id: "+idPokemon);
		return template.getForEntity("https://pokeapi.co/api/v2/pokemon/"+idPokemon+"/", Pokemon.class);
		//logger.info("Pokemones {} obtenido en la busqueda que no estan en la cache ", p.toString());
	}
	//Busqueda de pokemon mediante URL obtenida en la busqueda principal de pokemon
	public ResponseEntity<Pokemon> findPokemonByUrl(String url) {
		return template.getForEntity(url, Pokemon.class);
	}
	//Busqueda standard con paginacion de 20 maximo pokemon
	public ResponseEntity<NamedApiResourceList> findPokemonPaginacion() {
		return template.getForEntity("https://pokeapi.co/api/v2/pokemon/", NamedApiResourceList.class);
	}
	public ResponseEntity<NamedApiResourceList> findNextPokemonPaginacion() {
		return template.getForEntity("https://pokeapi.co/api/v2/pokemon/?offset=20&limit=20/", NamedApiResourceList.class);
	}
	
	public ResponseEntity<NamedApiResourceList> findPreviousPokemonPaginacion() {
		return template.getForEntity("https://pokeapi.co/api/v2/pokemon/?offset=0&limit=20/", NamedApiResourceList.class);
	}
	//Busqueda siguiente con url obtenida desde la busqueda principal de pokemones en general (https://pokeapi.co/api/v2/pokemon/?offset=20&limit=20/)
	@Cacheable(cacheNames = "pokemones", key="#url")
	public ResponseEntity<NamedApiResourceList> findNextPokemon(String url) {
		logger.info("Entro a buscar lista de pokemon con url: "+url);
		return template.getForEntity(url, NamedApiResourceList.class);
	}
	//Busqueda previa con url obtenida desde la busqueda principal de pokemones en general (https://pokeapi.co/api/v2/pokemon/?offset=0&limit=20/)
	@Cacheable(cacheNames = "pokemones", key="#url")
	public ResponseEntity<NamedApiResourceList> findPreviousPokemon(String url) {
		logger.info("Entro a buscar lista de pokemon con url: "+url);
		return template.getForEntity(url, NamedApiResourceList.class);
	}
	
	@CacheEvict(cacheNames = "pokemones", key = "#id")
	public String deletePokemon(String id) {
		return "Pokemon Eliminado";
	}
	
	public String getGuardarPokemon(int idPokemon, String idUsuario) {
		
		String sql = "select * from pokemon_seleccionados where id_user = '"+idUsuario+"'";
		//List<PokemonSelect> resulta;
		List<Map<String, Object>> resulta = jdbcTemplate.queryForList(sql);
		logger.info("Cantidad de pokemones guardados: "+resulta.size());
		if (resulta.size() < 3) {
			sql = "INSERT INTO pokemon_seleccionados (id_user, id_pokemon)values(?,?)";
			
			int result = jdbcTemplate.update(sql, idUsuario, idPokemon);
			
			if ( result>0) {
				return "Datos guardados";
			}else {
				return "No se pudo guardar el dato";
			}
		}else {
			return "No puede guardar mas Pokemon, Maximo 3";
		}
			
	}
	public List<Map<String, Object>> getObtenerPokemones(String idUsuario) {
		
		String sql = "select * from pokemon_seleccionados where id_user = '"+idUsuario+"'";
		//List<PokemonSelect> resulta;
		List<Map<String, Object>> resulta = jdbcTemplate.queryForList(sql);
		logger.info("Cantidad de pokemones guardados: "+resulta.size());
		
		return resulta;
			
	}
	public String getDeletePokemon(int idPokemon, String idUsuario) {
		
		String sql = "select * from pokemon_seleccionados where id_user = '"+idUsuario+"' and id_pokemon = '"+idPokemon+"'";

		List<Map<String, Object>> resulta = jdbcTemplate.queryForList(sql);
		logger.info("Pokemon encontrado: "+resulta.size());
		if (resulta.size() == 1) {
			sql = "delete from pokemon_seleccionados where id_user = '"+idUsuario+"' and id_pokemon = '"+idPokemon+"'";
			
			int result = jdbcTemplate.update(sql);
			
			if ( result>0) {
				return "Pokemon ["+idPokemon+"] a sido eliminado de la lista del usuario ["+idUsuario+"]";
			}else {
				return "No se pudo Eliminar el dato";
			}
		}else {
			return "No existe Pokemon ["+idPokemon+"] asociado al usuario ["+idUsuario+"]";
		}
			
	}
}
