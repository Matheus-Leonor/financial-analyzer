"""
API Bridge for Kotlin-Python Communication
Handles file processing and agent communication
"""

import os
import sys
import json
import argparse
from pathlib import Path
from typing import Dict, Any, Optional

from financial_agent import create_financial_agent

class FinancialAnalysisBridge:
    def __init__(self):
        """Initialize the bridge"""
        self.agent = None
        self.input_dir = "../shared-data/input"
        self.output_dir = "../shared-data/output"
        self.temp_dir = "../shared-data/temp"
        
        # Ensure directories exist
        for dir_path in [self.input_dir, self.output_dir, self.temp_dir]:
            os.makedirs(dir_path, exist_ok=True)
    
    def initialize_agent(self, api_key: Optional[str] = None) -> Dict[str, Any]:
        """Initialize the Claude agent"""
        try:
            self.agent = create_financial_agent(api_key)
            return {
                "status": "success",
                "message": "Agent initialized successfully"
            }
        except Exception as e:
            return {
                "status": "error",
                "message": f"Failed to initialize agent: {str(e)}"
            }
    
    def load_file(self, filename: str) -> Dict[str, Any]:
        """Load a file from the input directory"""
        if not self.agent:
            return {"status": "error", "message": "Agent not initialized"}
        
        file_path = os.path.join(self.input_dir, filename)
        
        if not os.path.exists(file_path):
            return {"status": "error", "message": f"File {filename} not found"}
        
        return self.agent.load_data(file_path)
    
    def process_chat(self, message: str) -> Dict[str, Any]:
        """Process a chat message"""
        if not self.agent:
            return {"status": "error", "message": "Agent not initialized"}
        
        return self.agent.chat(message)
    
    def get_data_summary(self) -> Dict[str, Any]:
        """Get summary of loaded data"""
        if not self.agent:
            return {"status": "error", "message": "Agent not initialized"}
        
        return self.agent.get_data_summary()
    
    def get_conversation_history(self) -> Dict[str, Any]:
        """Get conversation history"""
        if not self.agent:
            return {"status": "error", "message": "Agent not initialized"}
        
        return {
            "status": "success",
            "history": self.agent.get_conversation_history()
        }
    
    def clear_conversation(self) -> Dict[str, Any]:
        """Clear conversation history"""
        if not self.agent:
            return {"status": "error", "message": "Agent not initialized"}
        
        self.agent.clear_conversation()
        return {"status": "success", "message": "Conversation cleared"}
    
    def save_result(self, result: Dict[str, Any], filename: str = "result.json"):
        """Save result to output directory"""
        output_path = os.path.join(self.output_dir, filename)
        
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(result, f, indent=2, ensure_ascii=False, default=str)
        
        return output_path

def process_kotlin_request(request_file: str, response_file: str):
    """Process JSON request from Kotlin and save response"""
    bridge = FinancialAnalysisBridge()
    
    try:
        # Load request from JSON file
        with open(request_file, 'r', encoding='utf-8') as f:
            request_data = json.load(f)
        
        request_id = request_data.get('id', 'unknown')
        request_type = request_data.get('type', 'chat')
        message = request_data.get('message', '')
        file_data = request_data.get('fileData')
        
        # Initialize agent
        init_result = bridge.initialize_agent()
        if init_result["status"] == "error":
            response = {
                "id": request_id,
                "status": "error",
                "message": "Failed to initialize agent",
                "error": init_result["message"]
            }
        else:
            # Process based on request type
            if request_type == "load_data" and file_data:
                # Load file first, then process message
                file_path = file_data.get('path', '')
                if file_path:
                    load_result = bridge.agent.load_data(file_path)
                    if load_result["status"] == "success":
                        # Get data summary with pandas formatting
                        summary = bridge.get_data_summary()
                        if summary.get("loaded", False) and bridge.agent.chart_generator.current_data is not None:
                            df = bridge.agent.chart_generator.current_data
                            table_markdown = df.to_markdown(index=False)
                            table_string = df.to_string(index=False)
                            
                            response = {
                                "id": request_id,
                                "status": "success", 
                                "message": f"Dados carregados com sucesso!\n\n**Arquivo:** {file_data.get('name', 'Unknown')}\n**Linhas:** {df.shape[0]}\n**Colunas:** {df.shape[1]}\n\n{load_result.get('message', '')}",
                                "tableData": table_markdown,
                                "data": table_string
                            }
                        else:
                            response = {
                                "id": request_id,
                                "status": "error",
                                "message": "Failed to load data",
                                "error": load_result.get("message", "Unknown error")
                            }
                    else:
                        response = {
                            "id": request_id,
                            "status": "error", 
                            "message": "Failed to load file",
                            "error": load_result.get("message", "Unknown error")
                        }
                else:
                    response = {
                        "id": request_id,
                        "status": "error",
                        "message": "File path not provided"
                    }
            
            elif request_type == "chat":
                # Process chat message
                chat_result = bridge.process_chat(message)
                
                # Extract and clean the response message
                raw_response = chat_result.get("response", "")
                
                # Handle different response formats from Claude
                if isinstance(raw_response, list):
                    # If response is a list of message objects, extract text
                    cleaned_message = ""
                    for item in raw_response:
                        if isinstance(item, dict) and "text" in item:
                            cleaned_message += item["text"]
                        elif isinstance(item, str):
                            cleaned_message += item
                    response_message = cleaned_message
                elif isinstance(raw_response, dict):
                    # If response is a dict, try to extract text field
                    response_message = raw_response.get("text", str(raw_response))
                else:
                    # If response is already a string, use as-is
                    response_message = str(raw_response)
                
                # Check if response should include table formatting
                should_format_table = any(keyword in message.lower() for keyword in [
                    'tabela', 'relatório', 'compare', 'mostre dados', 'gere uma', 'liste', 'breakdown'
                ])
                
                table_data = None
                if should_format_table and bridge.agent.chart_generator.current_data is not None:
                    df = bridge.agent.chart_generator.current_data
                    table_data = df.to_markdown(index=False)
                
                response = {
                    "id": request_id,
                    "status": chat_result["status"],
                    "message": response_message,
                    "tableData": table_data,
                    "charts": chat_result.get("charts", []),
                    "error": chat_result.get("error")
                }
            
            else:
                response = {
                    "id": request_id,
                    "status": "error",
                    "message": f"Unknown request type: {request_type}"
                }
        
        # Save response to file
        with open(response_file, 'w', encoding='utf-8') as f:
            json.dump(response, f, indent=2, ensure_ascii=False, default=str)
            
        print(f"Request {request_id} processed successfully")
        
    except Exception as e:
        # Error response
        error_response = {
            "id": request_data.get('id', 'unknown') if 'request_data' in locals() else 'unknown',
            "status": "error",
            "message": f"Bridge processing error: {str(e)}",
            "error": str(e)
        }
        
        with open(response_file, 'w', encoding='utf-8') as f:
            json.dump(error_response, f, indent=2, ensure_ascii=False, default=str)
        
        print(f"Error processing request: {e}")

def main():
    """Main function - can handle both CLI and Kotlin JSON requests"""
    if len(sys.argv) == 3:
        # Called from Kotlin with request/response file paths
        request_file = sys.argv[1]
        response_file = sys.argv[2]
        process_kotlin_request(request_file, response_file)
        return
    
    # Original CLI interface
    parser = argparse.ArgumentParser(description="Financial Analysis Bridge")
    parser.add_argument("command", choices=["init", "load", "chat", "summary", "history", "clear"])
    parser.add_argument("--file", help="File to load (for load command)")
    parser.add_argument("--message", help="Chat message (for chat command)")
    parser.add_argument("--api-key", help="Anthropic API key")
    parser.add_argument("--output", default="result.json", help="Output filename")
    
    args = parser.parse_args()
    
    bridge = FinancialAnalysisBridge()
    
    if args.command == "init":
        result = bridge.initialize_agent(args.api_key)
    
    elif args.command == "load":
        if not args.file:
            result = {"status": "error", "message": "File parameter required for load command"}
        else:
            # Initialize agent first if not done
            if not bridge.agent:
                init_result = bridge.initialize_agent(args.api_key)
                if init_result["status"] == "error":
                    result = init_result
                else:
                    result = bridge.load_file(args.file)
            else:
                result = bridge.load_file(args.file)
    
    elif args.command == "chat":
        if not args.message:
            result = {"status": "error", "message": "Message parameter required for chat command"}
        else:
            # Initialize agent first if not done
            if not bridge.agent:
                init_result = bridge.initialize_agent(args.api_key)
                if init_result["status"] == "error":
                    result = init_result
                else:
                    result = bridge.process_chat(args.message)
            else:
                result = bridge.process_chat(args.message)
    
    elif args.command == "summary":
        result = bridge.get_data_summary()
    
    elif args.command == "history":
        result = bridge.get_conversation_history()
    
    elif args.command == "clear":
        result = bridge.clear_conversation()
    
    # Save result and print to stdout
    output_path = bridge.save_result(result, args.output)
    print(json.dumps(result, indent=2, default=str))

if __name__ == "__main__":
    main()