package com.icsd16191.bdgka_app.services;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Service;

import com.icsd16191.bdgka_app.entities.Block;
import com.icsd16191.bdgka_app.repos.BDGKARepository;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class ChaincodeService {
    private BDGKARepository repo; 
    public ChaincodeService(BDGKARepository repo){
      this.repo = repo;
    }
    public List<Block> getAllBlocks() throws Exception {
            var bytes = repo.getContract().evaluateTransaction("GetAllBlocks");
            var mapper = new ObjectMapper();
            return mapper.readValue(bytes, new TypeReference<List<Block>>() {});
    }
    public String appendBlock(Block block) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(block.getMs());
        
        String msJson = mapper.writeValueAsString(block.getMs());
        
        System.out.println("msJson "+msJson);
        System.out.println(block);
        repo.getContract().submitTransaction(
                "AppendBlock",
                block.getId(),
                block.getHeader(),
                block.getNextIp(),
                block.getHi2(),
                block.getPk(),
                msJson,
                block.getSignature()
        );
        return "OK";
    }
    public Block readBlock(String id) throws Exception {
            var bytes = repo.getContract().evaluateTransaction("ReadBlock",id);
            return new ObjectMapper().readValue(bytes, new TypeReference<Block>() {});
    }
    public Block getCurrent() throws Exception{
            var bytes = repo.getContract().evaluateTransaction("ReadCurrentBlock");
            return new ObjectMapper().readValue(bytes, new TypeReference<Block>() {});
      
    }
}
