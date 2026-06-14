package main

import (
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

type Block struct {
	ID          string   `json:"id"`
	Header      string   `json:"header"`
	NextIP      string   `json:"nextIp"`
	Hi2         string   `json:"hi2"`
	PK          string   `json:"pk"`
	Ms          []string   `json:"ms"`
	Signature   string   `json:"signature"`
	Timestamp   int64    `json:"timestamp"`
	PrevHash    string   `json:"prevHash"`
	CurrentHash string   `json:"currentHash"`
}

type BDGKAContract struct {
	contractapi.Contract
}

func (block *Block) hash() error {
	tempBlock := *block
	tempBlock.CurrentHash = ""
	tempBlockBytes, err := json.Marshal(tempBlock)
	if err != nil {
		return fmt.Errorf("failed to marshal block: %v", err)
	}
	hash_bytes := sha256.Sum256(tempBlockBytes)
	block.CurrentHash = hex.EncodeToString(hash_bytes[:])
	return nil
}
func (contract *BDGKAContract) AppendBlock(ctx contractapi.TransactionContextInterface,
	id string,
	header string,
	nextIP string,
	hi2 string,
	pk string,
	msJson string,
	signature string,
) error {
	var ms []string
	err := json.Unmarshal([]byte(msJson),&ms)
	if err != nil{
		return fmt.Errorf("json cannot be turned to list")
	}
	exists, err := contract.BlockExists(ctx, id)
	if err != nil {
		return err
	}
	if exists {
		return fmt.Errorf("block %s already exists", id)
	}

	timestamp, err := ctx.GetStub().GetTxTimestamp()
	if err != nil {
		return fmt.Errorf("timestamp error: %vs", err)
	}
	prevBlockIdBytes, err := ctx.GetStub().GetState("LAST_BLOCK")
	if err != nil {
		return fmt.Errorf("previous block error: %vs", err)
	}
	var prevBlock *Block
	var prevHash string
	if prevBlockIdBytes != nil {
		prevBlockId := string(prevBlockIdBytes)
		prevBlock, err = contract.ReadBlock(ctx, prevBlockId)
		if err != nil {
			return fmt.Errorf("previous block error: %v", err)
		}
		prevHash = prevBlock.CurrentHash
	} else {
		prevHash = "GENESIS"
	}
	block := Block{
		ID:        id,
		Header:    header,
		NextIP:    nextIP,
		Hi2:       hi2,
		PK:        pk,
		Ms:        ms,
		Signature: signature,
		Timestamp: timestamp.Seconds,
		PrevHash:  prevHash,
	}
	err = block.hash()
	if err != nil {
		return fmt.Errorf("error calculating signature: %v", err)
	}
	data, err := json.Marshal(block)
	if err != nil {
		return fmt.Errorf("invalid Ms JSON: %v", err)
	}
	if err := ctx.GetStub().PutState(id, data); err != nil {
		return fmt.Errorf("failed to put block state: %v", err)
	}
	if err := ctx.GetStub().PutState("LAST_BLOCK", []byte(id)); err != nil {
		return fmt.Errorf("failed to update LAST_BLOCK %v", err)
	}
	return nil
}

func (c *BDGKAContract) ReadBlock(ctx contractapi.TransactionContextInterface, id string) (*Block, error) {
	data, err := ctx.GetStub().GetState(id)
	if err != nil {
		return nil, fmt.Errorf("read: %v", err)
	}
	if data == nil {
		return nil, fmt.Errorf("block %s not found", id)
	}
	var block Block
	err = json.Unmarshal(data, &block)
	if err != nil {
		return nil, fmt.Errorf("unmarshal: %v", err)
	}
	return &block, nil
}

func (c *BDGKAContract) ReadCurrentBlock(ctx contractapi.TransactionContextInterface) (*Block, error) {

	prevBlockIdBytes, err := ctx.GetStub().GetState("LAST_BLOCK")
	prevBlockId := string(prevBlockIdBytes)
	block, err := c.ReadBlock(ctx, prevBlockId)
	if err != nil {
		return nil,fmt.Errorf("previous block error: %v", err)
	}
	return block, nil
}
func (c *BDGKAContract) BlockExists(ctx contractapi.TransactionContextInterface, id string) (bool, error) {
	data, err := ctx.GetStub().GetState(id)
	if err != nil {
		return false, err
	}
	return data != nil, nil
}
func (c *BDGKAContract) GetAllBlocks(ctx contractapi.TransactionContextInterface) ([]*Block, error) {
	resultsIterator, err := ctx.GetStub().GetStateByRange("", "")
	if err != nil {
		return nil, fmt.Errorf("failed to get state by range: %v", err)
	}
	defer resultsIterator.Close()
	var blocks []*Block
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, fmt.Errorf("failed to iterate results: %v", err)
		}
		key := queryResponse.Key
		if key == "LAST_BLOCK" {
			continue
		}
		var block Block
		err = json.Unmarshal(queryResponse.Value, &block)
		if err != nil {
			return nil, fmt.Errorf("failed to unmarshal block: %v", err)
		}
		blocks = append(blocks, &block)
	}
	return blocks, nil
}
func main() {
	cc, err := contractapi.NewChaincode(new(BDGKAContract))
	if err != nil {
		panic(fmt.Sprintf("initialization error: %v", err))
	}
	err = cc.Start()
	if err != nil {
		panic(fmt.Sprintf("error starting chaincode: %v", err))
	}
}
