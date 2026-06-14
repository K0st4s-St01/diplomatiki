package com.icsd16191.bdgka_app.rest;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.icsd16191.bdgka_app.entities.Block;
import com.icsd16191.bdgka_app.services.ChaincodeService;

@RestController
@RequestMapping("/master")
public class ContractController {
    private ChaincodeService service;

    public ContractController(ChaincodeService service){
        this.service=service;
    }

    
    @PostMapping("/append")
    public Map<String,Object> initializeParameters(@RequestBody Block block){
        try {
            return Map.of("result","OK","data",service.appendBlock(block));
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("result","ERROR","data",e.getMessage());
        }
    }

    @GetMapping("/current")
    public Map<String,Object> getCurrentBlock(){
        try {
            var block = service.getCurrent();
            return Map.of("result", "OK","data",block);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("result", "ERROR","data",e.getMessage());

        }
    }
    @GetMapping("/history")
    public Map<String,Object> history() {
        try {
            return Map.of("result","OK","data", service.getAllBlocks());
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("result", "ERROR","data",e.getMessage());
        }
    }

    @GetMapping("/block/{blockId}")
    public Map<String,Object> getBlock(@PathVariable("blockId") String id) {

        try {
            return Map.of("result","OK","data", service.readBlock(id));
        } catch (Exception e) {
            e.printStackTrace();
           return Map.of("result","ERROR","data",e.getMessage());
        }
    }
}
