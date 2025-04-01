package ru.etu.controlservice.entity;

public enum NodeType {
    SEGMENTATION_CT {
        @Override
        public Object getNodeStep(Node node) {
            return node.getCtSegmentation();
        }
    },
    SEGMENTATION_JAW {
        @Override
        public Object getNodeStep(Node node) {
            return node.getJawSegmentation();
        }
    },
    SEGMENTATION_ALIGNMENT {
        @Override
        public Object getNodeStep(Node node) {
            return node.getAlignmentSegmentation();
        }
    },
    RESULT_PLANNING {
        @Override
        public Object getNodeStep(Node node) {
            return node.getResultPlanning();
        }
    },
    TREATMENT_PLANNING {
        @Override
        public Object getNodeStep(Node node) {
            return node.getTreatmentPlanning();
        }
    };

    // Можно добавить новые типы, например:
// OTHER {
//     @Override
//     public Object getSegmentation(Node node) {
//         return node.getOtherSegmentation();
//     }
// }
    public abstract Object getNodeStep(Node node);

}