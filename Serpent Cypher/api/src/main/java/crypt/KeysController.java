package main.java.crypt;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Map;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@CrossOrigin(origins = "http://localhost:8090",
methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE },
maxAge = 3600)


@RestController
@SpringBootApplication
public class KeysController implements Serializable{
	
	@RequestMapping(value = {"/key", "/key/"},
			method = RequestMethod.GET,
			params = {"id"})
	public ResponseEntity<?> doGet(@RequestParam Map<String, String> params) {
		System.out.println(params);
		return new ResponseEntity<>("", HttpStatus.OK);
	}

}
