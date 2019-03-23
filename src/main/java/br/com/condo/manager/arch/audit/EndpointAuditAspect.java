package br.com.condo.manager.arch.audit;

import br.com.condo.manager.arch.model.entity.EndpointAudit;
import br.com.condo.manager.arch.model.entity.security.SecurityCredentials;
import br.com.condo.manager.arch.security.SecurityUtils;
import br.com.condo.manager.arch.service.EndpointAuditDAO;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Aspect
@Component
public class EndpointAuditAspect {

    protected Logger LOGGER = LoggerFactory.getLogger(EndpointAuditAspect.class);

    @Autowired
    SecurityUtils securityUtils;

    @Autowired
    private EndpointAuditDAO endpointAuditDAO;

    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping) || @annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.PutMapping) || @annotation(org.springframework.web.bind.annotation.DeleteMapping) || @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void mappedEndpointExecution(){}

    @Around("mappedEndpointExecution()")
    public Object AuditEndpointExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startingTime = System.currentTimeMillis();
        SecurityCredentials authenticatedCredentials = securityUtils.authenticatedCredentials();
        Long userId = authenticatedCredentials != null ? authenticatedCredentials.getId() : null;
        Date executionDate = new Date(startingTime);
        String action = joinPoint.getTarget().getClass().getSimpleName() + "#" + joinPoint.getSignature().getName();


        Object joinPointResult = joinPoint.proceed();
        if(!action.contains("GraphiQLController")) {
            if (joinPointResult == null || (joinPointResult instanceof ResponseEntity && !((ResponseEntity) joinPointResult).getStatusCode().isError())) {
                try {
                    long executionTime = System.currentTimeMillis() - startingTime;

                    Map<String, Object> map = getPayloadMap(joinPoint.getTarget().getClass(), joinPoint.getSignature().getName(), joinPoint.getArgs());
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
                    String payload = objectMapper.writeValueAsString(map);

                    EndpointAudit audit = endpointAuditDAO.create(new EndpointAudit(executionDate, userId, action, payload, executionTime));
                    LOGGER.info(audit.toString());
                } catch (Exception e) {
                    LOGGER.error("There was an error auditing the request: {userId: " + userId + ", action: " + action + "}", e);
                }
            }
        }

        return joinPointResult;
    }

    private Map<String, Object> getPayloadMap(Class endpointClass, String methodName, Object[] args) {
        Map<String, Object> map = new LinkedHashMap<>();
        if(args != null || args.length > 0) {
            List<Method> methods = Arrays.stream(endpointClass.getDeclaredMethods()).filter(m -> m.getName().equals(methodName)).collect(Collectors.toList());
            if(methods == null || methods.isEmpty()) {
                return getPayloadMap(endpointClass.getSuperclass(), methodName, args);
            } else {
                methods.stream().findFirst().ifPresent(method -> {
                    for(int paramIndex = 0; paramIndex < method.getParameters().length; paramIndex ++) {
                        String name = method.getParameters()[paramIndex].getName();
                        Object value = args[paramIndex];
                        if(value != null) {
                            if (value instanceof MultipartFile) {
                                MultipartFile file = (MultipartFile) value;
                                Map<String, Object> multipartData = new HashMap<>();
                                multipartData.put("originalFilename", file.getOriginalFilename());
                                multipartData.put("contentType", file.getContentType());
                                multipartData.put("size", file.getSize());
                                value = multipartData;
                            }
                            map.put(name, value);
                        }
                    }
                });
            }
        }
        return map;
    }

}
