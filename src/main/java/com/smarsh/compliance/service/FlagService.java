package com.smarsh.compliance.service;

import com.smarsh.compliance.entity.Flag;
import com.smarsh.compliance.repository.FlagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FlagService {
    private FlagRepository flagRepository;

    public FlagService(FlagRepository flagRepository) {
        this.flagRepository = flagRepository;
    }

    public void saveFlag(Flag flag) {
        System.out.println("trying to save"+flag);
        flagRepository.save(flag);
        log.info("Flag saved in DB");
    }
    public String getFlagSeverity(int violations)
    {
        if(violations <= 2)
        {
            return "LOW";
        }
        else if(violations <= 5)
        {
            return "MEDIUM";
        }
        else
        {
            return "HIGH";
        }
    }

}