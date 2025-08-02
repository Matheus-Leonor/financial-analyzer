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

def main():
    """Main function for command line usage"""
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