package com.xpert.persistence.query;

import com.xpert.persistence.exception.QueryFileNotFoundException;
import com.xpert.utils.StringUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author Ayslan
 */
public class QueryBuilder {

    private String order;
    private String attributeName;
    private Class from;
    private String alias;
    private StringBuilder joins = new StringBuilder();
    private List<Restriction> restrictions = new ArrayList<Restriction>();
    private QueryType type;
    private EntityManager entityManager;
    private static final Logger logger = Logger.getLogger(QueryBuilder.class.getName());

    public QueryBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public QueryBuilder(EntityManager entityManager, String alias) {
        this.entityManager = entityManager;
        this.alias = alias;
    }

    public QueryBuilder from(Class from) {
        this.from = from;
        if (alias == null || alias.isEmpty()) {
            alias = from.getSimpleName().substring(0, 1).toLowerCase();
        }
        return this;
    }

    public QueryBuilder from(Class from, String alias) {
        this.from = from;
        this.alias = alias;
        return this;
    }

    public QueryBuilder leftJoin(String join) {
        this.joins.append("LEFT JOIN ").append(join).append(" ");
        return this;
    }

    public QueryBuilder innerJoin(String join) {
        this.joins.append("INNER JOIN ").append(join).append(" ");
        return this;
    }

    public QueryBuilder join(String join) {
        this.joins.append("JOIN ").append(join).append(" ");
        return this;
    }

    public QueryBuilder leftJoinFetch(String join) {
        this.joins.append("LEFT JOIN FETCH ").append(join).append(" ");
        return this;
    }

    public QueryBuilder innerJoinFetch(String join) {
        this.joins.append("INNER JOIN FETCH ").append(join).append(" ");
        return this;
    }

    public QueryBuilder joinFetch(String join) {
        this.joins.append("JOIN FETCH ").append(join).append(" ");
        return this;
    }

    public QueryBuilder join(JoinBuilder joinBuilder) {
        if (joinBuilder != null) {
            this.alias = joinBuilder.getAlias();
            this.joins.append(joinBuilder);
        }
        return this;
    }

    public QueryBuilder orderBy(String order) {
        this.order = order;
        return this;
    }

    public QueryBuilder type(QueryType type) {
        this.type = type;
        return this;
    }

    public QueryBuilder type(QueryType type, String attributeName) {
        this.type = type;
        this.attributeName = attributeName;
        return this;
    }

    public QueryBuilder add(List<Restriction> restrictions) {
        if (restrictions != null) {
            this.restrictions.addAll(restrictions);
        }
        return this;
    }

    public QueryBuilder add(Map<String, Object> restrictions) {
        if (restrictions != null) {
            for (Map.Entry e : restrictions.entrySet()) {
                this.restrictions.add(new Restriction(e.getKey().toString(), e.getValue()));
            }
        }
        return this;
    }

    public QueryBuilder add(Restriction restriction) {
        if (restriction != null) {
            this.restrictions.add(restriction);
        }
        return this;
    }

    public QueryBuilder add(String field, Object value) {
        this.restrictions.add(new Restriction(order, value));
        return this;
    }

    public String getQueryString() {

        StringBuilder queryString = new StringBuilder();
        //type
        if (type == null) {
            type = QueryType.SELECT;
        }

        if (type.equals(QueryType.COUNT)) {
            queryString.append("SELECT COUNT(*) ");
        }

        if (type.equals(QueryType.MAX)) {
            queryString.append("SELECT MAX(").append(alias).append(".").append(attributeName).append(") ");
        }

        if (type.equals(QueryType.MIN)) {
            queryString.append("SELECT MIN(").append(alias).append(".").append(attributeName).append(") ");
        }

        if (type.equals(QueryType.SUM)) {
            queryString.append("SELECT SUM(").append(alias).append(".").append(attributeName).append(") ");
        }

        if (type.equals(QueryType.SELECT) && attributeName != null && !attributeName.isEmpty()) {
            queryString.append("SELECT ").append(attributeName).append(" ");
        }

        queryString.append("FROM ").append(from.getName()).append(" ");

        if (alias != null) {
            queryString.append(alias).append(" ");
        }

        if (joins != null) {
            queryString.append(joins).append(" ");
        }

        //restrictions
        int currentParameter = 1;
        boolean containsWhere = false;

        for (Restriction restriction : restrictions) {
            String propertyName;
            if (alias != null && !alias.trim().isEmpty()) {
                propertyName = alias + "." + restriction.getProperty();
            } else {
                propertyName = restriction.getProperty();
            }
            //if RestrictionType is null set default to EQUALS
            if (restriction.getRestrictionType() == null) {
                restriction.setRestrictionType(RestrictionType.EQUALS);
            }

            //LIKE type must be only for String
            if (restriction.getRestrictionType().equals(RestrictionType.LIKE) || restriction.getRestrictionType().equals(RestrictionType.NOT_LIKE)) {
                String property = "";
                if (alias != null && !alias.trim().isEmpty() && restriction.getProperty().indexOf(".") > -1) {
                    property = restriction.getProperty().substring(restriction.getProperty().indexOf(".") + 1, restriction.getProperty().length());
                } else {
                    property = restriction.getProperty();
                }
                try {
                    Class propertyType = ReflectionUtils.getPropertyType(this.from, property);
                    //set to type to EQUALS when is not String
                    if (!propertyType.equals(String.class)) {
                        restriction.setRestrictionType(RestrictionType.EQUALS);
                        if (propertyType.isEnum()) {
                            restriction.setValue(Enum.valueOf(propertyType, restriction.getValue().toString()));
                        }
                        if (propertyType.equals(Integer.class)) {
                            restriction.setValue(Integer.valueOf(StringUtils.getOnlyIntegerNumbers(restriction.getValue().toString())));
                        }
                        if (propertyType.equals(Long.class)) {
                            restriction.setValue(Long.valueOf(StringUtils.getOnlyIntegerNumbers(restriction.getValue().toString())));
                        }
                        if (propertyType.equals(BigDecimal.class)) {
                            restriction.setValue(new BigDecimal(restriction.getValue().toString()));
                        }
                    }
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Error getting Property Type: {0}", ex.getMessage());
                }
            }

            if (!containsWhere) {
                queryString.append("WHERE");
                containsWhere = true;
            } else {
                queryString.append("AND");
            }
            queryString.append(" ");
            if (restriction.getRestrictionType().equals(RestrictionType.LIKE) || restriction.getRestrictionType().equals(RestrictionType.NOT_LIKE)) {
                queryString.append("UPPER(").append(propertyName).append(")").append(" ");
            } else if (restriction.getTemporalType() != null && restriction.getTemporalType().equals(TemporalType.DATE)) {
                //force Date
                queryString.append("CAST(").append(propertyName).append(" AS date)").append(" ");
            } else {
                queryString.append(propertyName);
            }
            //if Value is null set default to IS NULL
            if (restriction.getValue() == null) {
                //  EQUALS null or IS_NULL
                if (restriction.getRestrictionType().equals(RestrictionType.EQUALS) || restriction.getRestrictionType().equals(RestrictionType.NULL)) {
                    queryString.append(" IS NULL ");
                } else if (restriction.getRestrictionType().equals(RestrictionType.NOT_NULL)) {
                    queryString.append(" IS NOT NULL ");
                }
            } else {
                queryString.append(" ").append(restriction.getRestrictionType().getSymbol()).append(" ");
                if (restriction.getRestrictionType().equals(RestrictionType.LIKE) || restriction.getRestrictionType().equals(RestrictionType.NOT_LIKE)) {
                    queryString.append("UPPER(?").append(currentParameter).append(")");
                } else if (restriction.getRestrictionType().equals(RestrictionType.IN) || restriction.getRestrictionType().equals(RestrictionType.NOT_IN)) {
                    queryString.append("(?").append(currentParameter).append(")");
                } else {
                    queryString.append("?").append(currentParameter);
                }
                queryString.append(" ");
                currentParameter++;
            }
        }
        //order by
        if (order != null && !order.trim().isEmpty()) {
            queryString.append("ORDER BY ").append(order).toString();
        }

        return queryString.toString();
    }

    public Query createQuery() {
        return createQuery(null);
    }

    public Query createQuery(Integer maxResults) {
        return createQuery(null, maxResults);
    }

    public Query createQuery(Integer firstResult, Integer maxResults) {

        String queryString = getQueryString();
        Query query = entityManager.createQuery(queryString);

        int parameter = 1;
        for (Restriction re : restrictions) {
            if (re.getValue() != null) {
                if (re.getRestrictionType().equals(RestrictionType.LIKE)) {
                    if (re.getLikeType() == null || re.getLikeType().equals(LikeType.BOTH)) {
                        query.setParameter(parameter, "%" + re.getValue() + "%");
                    } else if (re.getLikeType().equals(LikeType.BEGIN)) {
                        query.setParameter(parameter, "%" + re.getValue());
                    } else if (re.getLikeType().equals(LikeType.END)) {
                        query.setParameter(parameter, re.getValue() + "%");
                    }
                } else {
                    if (re.getTemporalType() != null && (re.getValue() instanceof Date || re.getValue() instanceof Calendar)) {
                        if (re.getValue() instanceof Date) {
                            query.setParameter(parameter, (Date) re.getValue(), re.getTemporalType());
                        } else if (re.getValue() instanceof Calendar) {
                            query.setParameter(parameter, (Calendar) re.getValue(), re.getTemporalType());
                        }
                    } else {
                        query.setParameter(parameter, re.getValue());
                    }
                }
                parameter++;
            }
        }

        if (maxResults != null) {
            query.setMaxResults(maxResults);
        }
        if (firstResult != null) {
            query.setFirstResult(firstResult);
        }
        return query;
    }

    public <T> List<T> getResultList(Integer maxResults) {
        List list = this.createQuery(maxResults).getResultList();
        if (list != null && attributeName != null && !attributeName.trim().isEmpty()) {
            return getNormalizedResultList(attributeName, list, from);
        }
        return list;
    }

    public <T> List<T> getResultList(Integer firstResult, Integer maxResults) {
        List list = this.createQuery(firstResult, maxResults).getResultList();
        if (list != null && attributeName != null && !attributeName.trim().isEmpty()) {
            return getNormalizedResultList(attributeName, list, from);
        }
        return list;
    }

    public <T> List<T> getResultList() {
        List list = this.createQuery().getResultList();
        if (list != null && attributeName != null && !attributeName.trim().isEmpty()) {
            return getNormalizedResultList(attributeName, list, from);
        }
        return list;
    }

    public static <T> List<T> getNormalizedResultList(String attributes, List resultList, Class<T> clazz) {
        if (attributes != null && attributes.split(",").length > 0) {
            List result = new ArrayList();
            String[] fields = attributes.split(",");
            for (Object object : resultList) {
                try {
                    Object entity = clazz.newInstance();
                    for (int i = 0; i < fields.length; i++) {
                        String property = fields[i].trim().replaceAll("/s", "");
                        initializeCascade(property, entity);
                        if (object instanceof Object[]) {
                            PropertyUtils.setProperty(entity, property, ((Object[]) object)[i]);
                        } else {
                            PropertyUtils.setProperty(entity, property, object);
                        }
                    }
                    result.add(entity);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
            return result;
        }
        return resultList;
    }

    public static void initializeCascade(String property, Object bean) {
        int index = property.indexOf(".");
        if (index > -1) {
            try {
                String field = property.substring(0, property.indexOf("."));
                Object propertyToInitialize = PropertyUtils.getProperty(bean, field);
                if (propertyToInitialize == null) {
                    propertyToInitialize = PropertyUtils.getPropertyDescriptor(bean, field).getPropertyType().newInstance();
                    PropertyUtils.setProperty(bean, field, propertyToInitialize);
                }
                String afterField = property.substring(index + 1, property.length());
                if (afterField != null && afterField.indexOf(".") > -1) {
                    initializeCascade(afterField, propertyToInitialize);
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    public static Query createNativeQueryFromFile(EntityManager entityManager, String queryPath, Class daoClass) {
        return createNativeQueryFromFile(entityManager, queryPath, daoClass, null);
    }

    public static Query createNativeQueryFromFile(EntityManager entityManager, String queryPath, Class daoClass, Class resultClass) {

        InputStream inputStream = daoClass.getResourceAsStream(queryPath);
        if (inputStream == null) {
            throw new QueryFileNotFoundException("Query File not found: " + queryPath + " in package: " + daoClass.getPackage());
        }
        try {
            String queryString = readInputStreamAsString(inputStream);
            if (resultClass != null) {
                return entityManager.createNativeQuery(queryString, resultClass);
            } else {
                return entityManager.createNativeQuery(queryString);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    public static String readInputStreamAsString(InputStream inputStream)
            throws IOException {

        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        bis.close();
        buf.close();
        return buf.toString();
    }

    public static void main(String[] args) {

        QueryBuilder builder =
                new QueryBuilder(null).from(Person.class).type(QueryType.MAX, "nome").leftJoin("p.group").leftJoin("p.profile").add(new Restriction("cpf", RestrictionType.LIKE, LikeType.BOTH)).add(new Restriction("nome", "maria")).orderBy("p.nome");

        builder.add(new Restriction("nome", null));

        System.out.println(builder.getQueryString());

    }
}
