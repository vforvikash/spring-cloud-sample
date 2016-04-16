package com.example;

import java.util.Arrays;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableDiscoveryClient
@SpringBootApplication
@EnableBinding(Sink.class)
public class ReservationServiceApplication {
	
	@Bean CommandLineRunner runner(ReservationRepository rr){
		return args -> {
			Arrays.asList("Vikash,Mintoo,Aakash,Hetal,Vidhi".split(",")).forEach(x-> rr.save(new Reservation(x)));
			rr.findAll().forEach(System.out::println);
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}

}

@MessageEndpoint
class MessageReservationReceiver{
	@Autowired ReservationRepository repo;
	
	@ServiceActivator(inputChannel = Sink.INPUT)
	public void acceptReservation(String rn){
		System.out.println("Received reservation name: "+rn);
		this.repo.save(new Reservation(rn));
	}
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long>{
	@RestResource (path="by-name")
	Collection<Reservation> findByReservationName(@Param("rn") String rn);
}

@RefreshScope
@RestController
class MessageRestController{
	@Value("${message}") private String message;
	
	@RequestMapping("/message")
	public String getMessage(){
		return this.message;
	}
}


@Entity
class Reservation{
	@Id
	@GeneratedValue
	private Long id;
	
	private String reservationName;
	
	public Reservation() {
	}
	
	public Reservation(String reservationName) {
		this.reservationName = reservationName;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the reservationName
	 */
	public String getReservationName() {
		return reservationName;
	}

	/**
	 * @param reservationName the reservationName to set
	 */
	public void setReservationName(String reservationName) {
		this.reservationName = reservationName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Reservation [id=" + id + ", reservationName=" + reservationName
				+ "]";
	}
}