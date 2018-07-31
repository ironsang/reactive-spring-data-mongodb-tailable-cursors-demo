package be.reactiveprogramming.demo.reactivespringdatatailablecursors.repository;

import be.reactiveprogramming.demo.reactivespringdatatailablecursors.data.ConcertTicket;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;

public interface ConcertTicketRepository extends ReactiveMongoRepository<ConcertTicket, Long> {

  @Tailable
  Flux<ConcertTicket> findWithTailableCursorBy();

}
