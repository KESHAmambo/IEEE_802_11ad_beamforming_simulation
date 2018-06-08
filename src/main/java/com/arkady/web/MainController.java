package com.arkady.web;

import com.arkady.simulation.SimulationService;
import com.arkady.model.SimulationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static java.lang.Thread.sleep;

/**
 * Created by abara on 03.05.2018.
 */

@Controller
public class MainController {
    @Autowired
    private SimulationService simulationService;

    @GetMapping("/star_simulation")
    @ResponseBody
    public Boolean startSimulation(Integer numberOfMobileStations) {
        SimulationConfig config = new SimulationConfig(numberOfMobileStations);
        simulationService.setConfig(config);
        return simulationService.startSimulation();
    }

    @GetMapping("/end_simulation")
    @ResponseBody
    public Boolean endSimulation() {
        simulationService.endSimulation();
        return true;
    }


    @GetMapping("/testSim")
    @ResponseBody
    public Boolean testSim() {
        /*long start = System.currentTimeMillis();
        try {
            sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Start " + start);
        long end = System.currentTimeMillis();
        System.out.println("Passed " + (end - start));*/

        return true;
    }
}
