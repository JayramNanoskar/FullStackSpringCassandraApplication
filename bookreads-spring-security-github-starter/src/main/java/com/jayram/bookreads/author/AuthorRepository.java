package com.jayram.bookreads.author;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

//To fetch & persist data in Cassandra
@Repository
public interface AuthorRepository extends CassandraRepository<Author, String> {
    

}
