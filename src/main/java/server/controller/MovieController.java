package server.controller;

import action.LoginAction;
import action.MovieAction;
import data.beans.MovieEntity;
import exceptions.MediaAlreadyReturnedException;
import exceptions.MediaNotFoundException;
import exceptions.UnavailableMediaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for movie management
 */
@RestController
public class MovieController {
    MovieAction movieAction = new MovieAction();

    /**
     * Get a specific movie
     * @param id ID of the movie
     * @return Movie if present
     */
    @RequestMapping(method = RequestMethod.GET, value = "/movie/{id}")
    public ResponseEntity<MovieEntity> getMovie(@PathVariable int id) {
        Optional<MovieEntity> movie = movieAction.getMovie(id);
        if (movie.isPresent()) {
            return new ResponseEntity<>(movie.get(), HttpStatus.OK) ;
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Return movies matching filter
     * @param filter Search term
     * @return List of movies
     */
    @RequestMapping(method = RequestMethod.GET, value = "/movie")
    public List<MovieEntity> searchMovies(@RequestParam(value="filter", defaultValue="", required=false) String filter) {
        return movieAction.searchMovies(filter);
    }

    /**
     * Add a new movie
     * @param movie Movie to add
     * @return Movie, if added
     */
    @RequestMapping(method = RequestMethod.POST, value = "/movie")
    public ResponseEntity<MovieEntity> addMovie(@RequestBody MovieEntity movie, @RequestParam String token) {
        if (LoginAction.isValidToken(token)) {
            movieAction.addMovie(movie);
            int id = movie.getId();
            Optional<MovieEntity> persistedMovie = movieAction.getMovie(id);
            if (persistedMovie.isPresent()) {
                return new ResponseEntity<>(persistedMovie.get(), HttpStatus.ACCEPTED);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Borrow a movie if available
     * @param id ID of the movie to borrow
     * @param username Username of the borrower
     * @return Movie, if borrowed
     */
    @RequestMapping(method = RequestMethod.POST, value = "/movie/{id}/borrow")
    public ResponseEntity<MovieEntity> borrowMovie(@PathVariable int id, @RequestParam(value="username") String username, @RequestParam String token) {
        if (LoginAction.isValidToken(token)) {
            try {
                movieAction.borrowMovie(id, username);
                Optional<MovieEntity> movie = movieAction.getMovie(id);
                if (movie.isPresent()) {
                    return new ResponseEntity<>(movie.get(), HttpStatus.ACCEPTED);
                } else {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            } catch (MediaNotFoundException e) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } catch (UnavailableMediaException e) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Return a borrowed movie
     * @param id ID of the movie
     * @param username Username of the borrower
     * @return Movie, if returned
     */
    @RequestMapping(method = RequestMethod.POST, value = "/movie/{id}/return")
    public ResponseEntity<MovieEntity> returnMovie(@PathVariable int id, @RequestParam(value="username") String username, @RequestParam String token) {
        if (LoginAction.isValidToken(token)) {
            try {
                movieAction.returnMovie(id, username);
                Optional<MovieEntity> movie = movieAction.getMovie(id);
                if (movie.isPresent()) {
                    return new ResponseEntity<>(movie.get(), HttpStatus.ACCEPTED);
                } else {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            } catch (MediaNotFoundException e) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } catch (MediaAlreadyReturnedException e) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
