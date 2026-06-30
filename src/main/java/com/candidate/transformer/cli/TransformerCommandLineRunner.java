package com.candidate.transformer.cli;

import com.candidate.transformer.service.TransformerService;
import com.candidate.transformer.util.ConsoleLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * CLI command-line runner that handles startup arguments and kicks off the processing flow.
 */
@Component
@Order(2)
public class TransformerCommandLineRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TransformerCommandLineRunner.class);

    private final TransformerService transformerService;

    public TransformerCommandLineRunner(TransformerService transformerService) {
        this.transformerService = transformerService;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Candidate Profile Transformer Command Line Interface has started.");
        
        // Safety check to verify if CLI is running in interactive mode
        boolean interactive = System.console() != null;
        for (String arg : args) {
            if ("--batch".equalsIgnoreCase(arg) || "--test".equalsIgnoreCase(arg)) {
                interactive = false;
                break;
            }
        }

        if (interactive) {
            System.out.println("================================================================================");
            System.out.println("   CANDIDATE PROFILE TRANSFORMER");
            System.out.println("================================================================================");
            System.out.println("1. Run Complete Transformation Pipeline");
            System.out.println("2. Exit");
            System.out.print("\nSelect option [1-2] (default: 1): ");
            
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String choice = reader.readLine();
                if ("2".equals(choice != null ? choice.trim() : "")) {
                    ConsoleLogger.logInfo("Exiting application.");
                    return;
                }
            } catch (Exception e) {
                logger.warn("Interactive input failed, falling back to batch execution", e);
            }
        }

        try {
            transformerService.process();
        } catch (Exception e) {
            logger.error("Error occurred during candidate transformation process execution", e);
            ConsoleLogger.logWarning("Transformation failed: " + e.getMessage());
        }
    }
}
