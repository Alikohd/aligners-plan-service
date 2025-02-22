package ru.etu.controlservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.etu.controlservice.service.PacsService;


@SpringBootTest
class PacsTest {

    @Autowired
    private PacsService pacsService;

}
