package skaro.pokemonapi.controlador;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import skaro.pokemonapi.servicios.PokemonServicio;

@RestController
@RequestMapping("/soporte")
public class SoporteController  {
	private final CacheManager cacheManager ;
	private Logger logger = LoggerFactory.getLogger(PokemonServicio.class);
	public SoporteController (CacheManager cachema) {
		this.cacheManager = cachema;
	}
	

	
	@GetMapping
	public void playingWithCache(String userId) {
		ConcurrentHashMap<?, ?> cache = (ConcurrentHashMap<?, ?>) this.cacheManager.getCache("pokemones"+userId).getNativeCache();
		
		//Obtener elementos
		cache.forEach((key,value)-> {
			logger.info("Key is " + key + " and value "+ value.toString());
		});
	}


}
