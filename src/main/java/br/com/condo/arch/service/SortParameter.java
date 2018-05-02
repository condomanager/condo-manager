package br.com.condo.arch.service;

public class SortParameter {

    public enum Order {
        ASC,
        DESC
    }

    private String attribute;
    private Order order;

    public SortParameter(String attribute, Order order) {
        this.attribute = attribute;
        this.order = order;
    }

    public SortParameter(String parameter) {
        boolean isDesc = parameter.startsWith("-");
        String cleanParameter = isDesc ? parameter.replaceFirst("-", "") : parameter;
        this.attribute = cleanParameter;
        this.order = isDesc ? Order.DESC : Order.ASC;
    }

    public String getAttribute() {
        return attribute;
    }

    public Order getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        SortParameter other = (SortParameter) object;

        return attribute != null ? attribute.equals(other.attribute) : other.attribute == null;
    }

    @Override
    public int hashCode() {
        return attribute != null ? attribute.hashCode() : 0;
    }
}
