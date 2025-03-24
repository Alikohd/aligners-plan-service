package ru.etu.controlservice.util;

import ru.etu.controlservice.entity.Node;

public enum SegmentationTypeRequiredForAlignment {
    CT {
        @Override
        public Object getSegmentation(Node node) {
            return node.getCtSegmentation();
        }
    },
    JAW {
        @Override
        public Object getSegmentation(Node node) {
            return node.getJawSegmentation();
        }
    };
    // Можно добавить новые типы, например:
    // OTHER {
    //     @Override
    //     public Object getSegmentation(Node node) {
    //         return node.getOtherSegmentation();
    //     }
    // }
    public abstract Object getSegmentation(Node node);

}