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
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
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

import com.example.bookmark.Account;
import com.example.bookmark.AccountRepository;
import com.example.bookmark.Bookmark;
import com.example.bookmark.BookmarkRepository;

/**
 * a service, registered to eureka server.
 * @author vikash.kaushik
 *
 */
@EnableDiscoveryClient
@SpringBootApplication
@EnableBinding(Sink.class)//Added for stream to receive data using message channel
public class ReservationServiceApplication {
	
	/**
	 * @return Its needed for sending status as sample to spring sleuth
	 */
	@Bean AlwaysSampler alwaysSampler(){
		return new AlwaysSampler();
	}
	
	/**
	 * command line runner to initiate some data
	 * @param rr
	 * @return
	 */
	@Bean CommandLineRunner runner(ReservationRepository rr){
		return args -> {
			Arrays.asList("Vikash,Mintoo,Aakash,Hetal,Vidhi".split(",")).forEach(x-> rr.save(new Reservation(x)));
			rr.findAll().forEach(System.out::println);
		};
	}
	
	/**
	 * @param accountRepository
	 * @param bookmarkRepository
	 * @return CommandLineRunner
	 */
	@Bean
	CommandLineRunner initBookmarkData(AccountRepository accountRepository,
			BookmarkRepository bookmarkRepository) {
		return (evt) -> Arrays.asList(
				"jhoeller,dsyer,pwebb,ogierke,rwinch,mfisher,mpollack,jlong".split(","))
				.forEach(
						a -> {
							Account account = accountRepository.save(new Account(a,
									"password"));
							bookmarkRepository.save(new Bookmark(account,
									"http://bookmark.com/1/" + a, "A description"));
							bookmarkRepository.save(new Bookmark(account,
									"http://bookmark.com/2/" + a, "A description"));
						});
	}

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}

}

/**
 * Message channel listener to get data from client
 * @author vikash.kaushik
 *
 */
@MessageEndpoint
class MessageReservationReceiver{
	@Autowired ReservationRepository repo;
	
	@ServiceActivator(inputChannel = Sink.INPUT)
	public void acceptReservation(String rn){
		System.out.println("Received reservation name: "+rn);
		this.repo.save(new Reservation(rn));
	}
}

/**
 * Rest resource
 * @author vikash.kaushik
 *
 */
@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long>{
	@RestResource (path="by-name")
	Collection<Reservation> findByReservationName(@Param("rn") String rn);
}

/**
 * to check refreshscope implementation. actuator in action + update in config if property is changed in config server
 * @author vikash.kaushik
 *
 */
@RefreshScope
@RestController
class MessageRestController{
	@Value("${message}") private String message;
	
	@RequestMapping("/message")
	public String getMessage(){
		return this.message;
	}
}


/**
 * sample entity
 * @author vikash.kaushik
 *
 */
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