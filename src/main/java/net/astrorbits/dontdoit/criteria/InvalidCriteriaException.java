package net.astrorbits.dontdoit.criteria;

public class InvalidCriteriaException extends RuntimeException {
    public InvalidCriteriaException(Criteria criteria, String message) {
        super("[Criteria: %s] %s".formatted(criteria.getType().name(), message));
    }
}
