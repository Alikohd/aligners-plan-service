package ru.etu.controlservice.entity;

import java.util.function.Predicate;

public enum NodeType {
    SEGMENTATION_CT(node -> node.getCtSegmentation() != null) {
        @Override
        public Object getNodeStep(Node node) {
            return node.getCtSegmentation();
        }
    },
    SEGMENTATION_JAW(node -> node.getJawSegmentation() != null) {
        @Override
        public Object getNodeStep(Node node) {
            return node.getJawSegmentation();
        }
    },
    SEGMENTATION_ALIGNMENT(node -> node.getAlignmentSegmentation() != null) {
        @Override
        public Object getNodeStep(Node node) {
            return node.getAlignmentSegmentation();
        }
    },
    RESULT_PLANNING(node -> node.getResultPlanning() != null) {
        @Override
        public Object getNodeStep(Node node) {
            return node.getResultPlanning();
        }
    },
    TREATMENT_PLANNING(node -> node.getTreatmentPlanning() != null) {
        @Override
        public Object getNodeStep(Node node) {
            return node.getTreatmentPlanning();
        }
    },
    EMPTY_NODE(node -> false) {
        @Override
        public Object getNodeStep(Node node) {
            return null;
        }
    };

    // Можно добавить новые типы, например:
// OTHER {
//     @Override
//     public Object getSegmentation(Node node) {
//         return node.getOtherSegmentation();
//     }
// }
    private final Predicate<Node> predicate;

    NodeType(Predicate<Node> predicate) {
        this.predicate = predicate;
    }

    public boolean test(Node node) {
        return predicate.test(node);
    }

    public abstract Object getNodeStep(Node node);
}