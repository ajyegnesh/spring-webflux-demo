package com.howtodoinjava.demo.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.howtodoinjava.demo.dao.EmployeeRepository;
import com.howtodoinjava.demo.model.Employee;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.util.context.Context;

import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Service
public class EmployeeService implements IEmployeeService {


    private static Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    EmployeeRepository employeeRepo;

    public void create(Employee e) {
        employeeRepo.save(e).subscribe();
    }

    public Mono<Employee> findById(Integer id) {
        return employeeRepo.findById(id).doOnEach(logOnNextInteger(r -> logger.debug("Employee details for the employee ${}", r.getName()))).subscriberContext(Context.of("apiID", id));
    }

//    public Flux<Employee> findByName(String name) {
//        return employeeRepo.findByName(name);
//    }

    public Flux<Employee> findByName(String name) {
        return employeeRepo.findByName(name).doOnEach(logOnNext(r -> logger.debug("Employee details for the employee ${}", r.getId()))).subscriberContext(Context.of("apiID", name));
    }

    public Flux<Employee> findAll() {
        return employeeRepo.findAll().doOnEach((r -> logger.debug("Statement is printed", r.getType())));
    }


    private static <T> Consumer<Signal<T>> logOnNext(Consumer<T> logStatement) {
        return signal -> {
            if (!signal.isOnNext()) return;
            Optional<String> apiIDMaybe = signal.getContext().getOrEmpty("apiID");

            apiIDMaybe.ifPresent(apiID -> {
                try (MDC.MDCCloseable closeable = MDC.putCloseable("apiID", apiID)) {
                    logStatement.accept(signal.get());
                }
            });
        };
    }


    private static <T> Consumer<Signal<T>> logOnNextInteger(Consumer<T> logStatement) {
        return signal -> {
            if (!signal.isOnNext()) return;
            Optional<Integer> apiIDMaybe = signal.getContext().getOrEmpty("apiID");

            apiIDMaybe.ifPresent(apiID -> {
                try (MDC.MDCCloseable closeable = MDC.putCloseable("apiID", String.valueOf(apiID))) {
                    logStatement.accept(signal.get());
                }
            });
        };
    }

    public Mono<Employee> update(Employee e) {
        return employeeRepo.save(e);
    }

    public Mono<Void> delete(Integer id) {
        return employeeRepo.deleteById(id);
    }

}