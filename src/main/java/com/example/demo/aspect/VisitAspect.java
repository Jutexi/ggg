package com.example.demo.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import com.example.demo.service.VisitService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class VisitAspect {

    private final VisitService visitService;

    public VisitAspect(VisitService visitService) {
        this.visitService = visitService;
    }

    @Before("execution(* com.example.demo.controller..*(..))")
    public void registerVisit() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String uri = request.getRequestURI();
        visitService.registerVisit(uri);
    }
}