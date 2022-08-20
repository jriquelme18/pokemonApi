package skaro.pokemonapi.controlador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import skaro.pokeapi.resource.NamedApiResourceList;
import skaro.pokeapi.resource.pokemon.Pokemon;
import skaro.pokemonapi.servicios.PokemonServicio;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/pokemon")
public class PokemonControlador {
	@Autowired
	private PokemonServicio pokemonServicio;
	private final CacheManager cacheManager ;
	private Logger logger = LoggerFactory.getLogger(PokemonServicio.class);
	private int count = 0;
	public PokemonControlador (CacheManager cachema) {
		this.cacheManager = cachema;
	}
	@GetMapping("/complete")
	public ResponseEntity<Pokemon> getFirstPokemon() {
		return pokemonServicio.findFirstPokemon();
	}
	
	@GetMapping("/name")
	public ResponseEntity<Pokemon> getPokemonById(String idPokemon, String userId) {
		ConcurrentHashMap<?, ?> cache = (ConcurrentHashMap<?, ?>) this.cacheManager.getCache("pokemones").getNativeCache();
		
		//Obtener elementos
		cache.forEach((key,value)-> {
			this.count++;
		});
		if (count == 3 ){
			logger.info("no podemos seleccionar mas pokemon -> count is " + count );
			count =0;
			return null;
			
		}else {
			logger.info("count is " + count );
			count =0;
			return pokemonServicio.findPokemonById(idPokemon,userId);
		}
	}
	@GetMapping("/pokeurl")
	public ResponseEntity<Pokemon> getPokemonByUrl(String url) {
		return pokemonServicio.findPokemonByUrl(url);
	}
	@GetMapping("/searchAll")
	public ResponseEntity<NamedApiResourceList> getPokemonPaginacion() {
		return pokemonServicio.findPokemonPaginacion();
	}
	@GetMapping("/nextTest")
	public ResponseEntity<NamedApiResourceList> getNextPokemonPaginacion() {
		return pokemonServicio.findNextPokemonPaginacion();
	}
	@GetMapping("/previousTest")
	public ResponseEntity<NamedApiResourceList> getPreviousPokemonPaginacion() {
		return pokemonServicio.findPreviousPokemonPaginacion();
	}
	@GetMapping("/next")
	public ResponseEntity<NamedApiResourceList> getNextPokemon(String url) {
		return pokemonServicio.findNextPokemon(url);
	}
	@GetMapping("/previous")
	public ResponseEntity<NamedApiResourceList> getPreviousPokemon(String url) {
		return pokemonServicio.findPreviousPokemon(url);
	}
	@DeleteMapping("/{id}")
	public String deletePokemon(@PathVariable String id) {
		return pokemonServicio.deletePokemon(id);
	}
	@GetMapping("/guardar")
	public String getGuardarPokemon(int idPokemon, String idUsuario) {
		return pokemonServicio.getGuardarPokemon(idPokemon, idUsuario);
	}
	@GetMapping("/obtenerLista")
	public List<Map<String, Object>> getObtenerPokemones(String idUsuario){
		return pokemonServicio.getObtenerPokemones(idUsuario);
	}
	@GetMapping("/eliminarPokemon")
	public String getDeletePokemon(int idPokemon, String idUsuario) {
		return pokemonServicio.getDeletePokemon(idPokemon, idUsuario);
	}
}
