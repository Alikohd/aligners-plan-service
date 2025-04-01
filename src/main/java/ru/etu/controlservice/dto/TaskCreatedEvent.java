package ru.etu.controlservice.dto;

import ru.etu.controlservice.entity.Task;

public record TaskCreatedEvent(Task task) {
}
