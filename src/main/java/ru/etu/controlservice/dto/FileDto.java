package ru.etu.controlservice.dto;

import org.springframework.core.io.Resource;

public record FileDto(String name, Resource content) {
}