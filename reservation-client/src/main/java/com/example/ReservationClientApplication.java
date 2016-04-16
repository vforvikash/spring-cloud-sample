package com.example;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}
	
}

@EnableBinding(Source.class)
@RestController
@RequestMapping("/reservations")
class ReservationApiRestController{
	
	@Autowired
	@LoadBalanced 
	private RestTemplate restTemplate; 
	
	@Autowired
	@Output(Source.OUTPUT)
	private MessageChannel messageChannel;
	
	ParameterizedTypeReference<Resources<Reservation>> ptr = new  ParameterizedTypeReference<Resources<Reservation>>() {};
	
	@RequestMapping(method=RequestMethod.POST)
	public void write(@RequestBody Reservation reservation){
		System.out.println("Received reservation request: "+reservation.toString());
		this.messageChannel.send(MessageBuilder.withPayload(reservation.getReservationName()).build());
	}
	
	@RequestMapping("/names")
	public Collection<String> getReservationNames(){
		ResponseEntity<Resources<Reservation>> responseEntity = this.restTemplate.exchange("http://reservation-service/reservations", HttpMethod.GET, null, ptr);
		return responseEntity.getBody()
				.getContent().stream().map(Reservation::getReservationName).collect(Collectors.toList());
	}
}

@Configuration
class MyConfiguration {

    @LoadBalanced
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}


class Reservation{
	private Long id;
	private String reservationName;
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