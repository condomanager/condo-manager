package br.com.condo.arch.service;

public class SearchParameter {

    public enum Operator {
        EQUAL("==:"),
        NOT_EQUAL("!=:"),
        LIKE("=:"),
        GREATER_THAN(">:"),
        GREATER_THAN_OR_EQUAL_TO(">=:"),
        LESS_THAN("<:"),
        LESS_THAN_OR_EQUAL_TO("<=:"),
        BETWEEN("><:"),
        IN("in:"),
        NOT_IN("!in:"),
        IS_NULL("null"),
        IS_NOT_NULL("!null");

        private String value;

        Operator(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Operator byValue(String value) {
            for (Operator userType : Operator.values()) {
                if (userType.getValue().equalsIgnoreCase(value)) {
                    return userType;
                }
            }
            throw new IllegalArgumentException("Invalid operator value");
        }
    }

    public static final String OPERATORS_REGEX = Operator.EQUAL.getValue() + "|" +
            Operator.NOT_EQUAL.getValue() + "|" +
            Operator.LIKE.getValue() + "|" +
            Operator.GREATER_THAN.getValue() + "|" +
            Operator.GREATER_THAN_OR_EQUAL_TO.getValue() + "|" +
            Operator.LESS_THAN.getValue() + "|" +
            Operator.LESS_THAN_OR_EQUAL_TO.getValue() + "|" +
            Operator.BETWEEN.getValue() + "|" +
            Operator.IN.getValue() + "|" +
            Operator.NOT_IN.getValue() + "|" +
            Operator.IS_NULL.getValue() + "|" +
            Operator.IS_NOT_NULL.getValue();

    private String attribute;
    private Operator operator;
    private Object value;

    public SearchParameter(String attribute, Operator operator, Object value) {
        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
    }

    public SearchParameter(String attribute, Operator operator) {
        this(attribute, operator, null);
    }

    public String getAttribute() {
        return attribute;
    }

    public Operator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        SearchParameter other = (SearchParameter) object;

        return attribute != null ? attribute.equals(other.attribute) : other.attribute == null;
    }

    @Override
    public int hashCode() {
        return attribute != null ? attribute.hashCode() : 0;
    }
}
