package org.example.fileuploader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FileuploaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileuploaderApplication.class, args);
	}

}
