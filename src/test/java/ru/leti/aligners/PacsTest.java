package ru.leti.aligners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.leti.aligners.service.PacsService;


@SpringBootTest
class PacsTest {

    @Autowired
    private PacsService pacsService;

}
