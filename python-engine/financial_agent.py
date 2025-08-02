"""
Financial Analysis Agent with Claude 4.0 and Chart Generation
Powered by LangChain and Anthropic Claude
"""

import os
import json
from typing import List, Dict, Any, Optional
from dotenv import load_dotenv

from langchain_anthropic import ChatAnthropic
from langchain.agents import create_tool_calling_agent, AgentExecutor
from langchain.memory import ConversationBufferMemory
from langchain_core.prompts import ChatPromptTemplate
from langchain.schema import HumanMessage, AIMessage

from chart_tools import ChartGenerator, create_chart_tools

# Load environment variables
load_dotenv()

class FinancialAnalysisAgent:
    def __init__(self, anthropic_api_key: Optional[str] = None):
        """Initialize the Financial Analysis Agent with Claude 4.0"""
        
        # Setup API key
        api_key = anthropic_api_key or os.getenv("ANTHROPIC_API_KEY")
        if not api_key:
            raise ValueError("ANTHROPIC_API_KEY not found in environment variables or parameters")
        
        # Initialize Claude 4.0 (Sonnet)
        self.llm = ChatAnthropic(
            model="claude-3-5-sonnet-20241022",  # Claude 4.0 Sonnet
            temperature=0.1,  # Low temperature for consistent analysis
            max_tokens=4000,
            api_key=api_key
        )
        
        # Initialize chart generator
        self.chart_generator = ChartGenerator()
        
        # Create tools
        self.tools = create_chart_tools(self.chart_generator)
        
        # Initialize memory for conversation
        self.memory = ConversationBufferMemory(
            memory_key="chat_history",
            return_messages=True
        )
        
        # Create the agent
        self.agent = self._create_agent()
        
        # Create agent executor
        self.agent_executor = AgentExecutor(
            agent=self.agent,
            tools=self.tools,
            memory=self.memory,
            verbose=True,
            max_iterations=5,
            early_stopping_method="force"
        )
    
    def _create_agent(self):
        """Create the LangChain agent with Claude 4.0"""
        
        system_prompt = """You are a Financial Analysis AI Assistant powered by Claude 4.0. 
        You specialize in analyzing financial data and creating insightful visualizations.

        Your capabilities include:
        1. Loading and analyzing CSV/Excel financial data
        2. Generating various types of charts (bar, line, pie, heatmap)
        3. Providing financial insights and recommendations
        4. Answering questions about loaded data

        When users ask for charts or visualizations:
        1. First, understand what type of chart would best represent their request
        2. Use the appropriate chart generation tool
        3. Provide context and insights about the generated chart

        Chart types and when to use them:
        - Bar charts: For comparing categories (e.g., "revenue by month", "expenses by department")
        - Line charts: For trends over time (e.g., "profit growth", "stock price movement")
        - Pie charts: For showing distribution/percentages (e.g., "expense breakdown", "market share")
        - Heatmaps: For correlation analysis (e.g., "relationship between variables")

        Always be helpful, accurate, and provide actionable financial insights.
        If you generate a chart, explain what it shows and what insights can be drawn from it.
        """
        
        prompt = ChatPromptTemplate.from_messages([
            ("system", system_prompt),
            ("placeholder", "{chat_history}"),
            ("human", "{input}"),
            ("placeholder", "{agent_scratchpad}")
        ])
        
        return create_tool_calling_agent(
            llm=self.llm,
            tools=self.tools,
            prompt=prompt
        )
    
    def load_data(self, file_path: str) -> Dict[str, Any]:
        """Load financial data from file"""
        success = self.chart_generator.load_data(file_path)
        
        if success:
            return {
                "status": "success",
                "message": f"Data loaded successfully from {file_path}",
                "data_info": self.chart_generator.data_context
            }
        else:
            return {
                "status": "error",
                "message": f"Failed to load data from {file_path}"
            }
    
    def chat(self, user_message: str) -> Dict[str, Any]:
        """Process user message and return AI response with any generated charts"""
        try:
            # Execute the agent
            response = self.agent_executor.invoke({
                "input": user_message
            })
            
            # Check for newly generated charts
            chart_files = self._get_recent_charts()
            
            return {
                "status": "success",
                "response": response.get("output", ""),
                "charts": chart_files
            }
            
        except Exception as e:
            return {
                "status": "error",
                "response": f"Error processing request: {str(e)}",
                "charts": []
            }
    
    def _get_recent_charts(self) -> List[str]:
        """Get list of recently generated chart files"""
        try:
            output_dir = self.chart_generator.output_dir
            if not os.path.exists(output_dir):
                return []
            
            # Get all PNG files (charts) from output directory
            chart_files = [f for f in os.listdir(output_dir) if f.endswith('.png')]
            
            # Sort by creation time, return most recent
            chart_files = sorted(chart_files, 
                               key=lambda x: os.path.getctime(os.path.join(output_dir, x)),
                               reverse=True)
            
            return chart_files[:5]  # Return up to 5 most recent charts
            
        except Exception:
            return []
    
    def get_conversation_history(self) -> List[Dict[str, str]]:
        """Get formatted conversation history"""
        history = []
        
        for message in self.memory.chat_memory.messages:
            if isinstance(message, HumanMessage):
                history.append({"role": "user", "content": message.content})
            elif isinstance(message, AIMessage):
                history.append({"role": "assistant", "content": message.content})
        
        return history
    
    def clear_conversation(self):
        """Clear conversation memory"""
        self.memory.clear()
    
    def get_data_summary(self) -> Dict[str, Any]:
        """Get summary of currently loaded data"""
        if self.chart_generator.current_data is not None:
            return {
                "loaded": True,
                "shape": self.chart_generator.current_data.shape,
                "columns": list(self.chart_generator.current_data.columns),
                "data_types": self.chart_generator.current_data.dtypes.to_dict()
            }
        else:
            return {"loaded": False}

# Helper function for external usage
def create_financial_agent(anthropic_api_key: Optional[str] = None) -> FinancialAnalysisAgent:
    """Create and return a FinancialAnalysisAgent instance"""
    return FinancialAnalysisAgent(anthropic_api_key)

if __name__ == "__main__":
    # Test the agent
    print("Testing Financial Analysis Agent with Claude 4.0...")
    
    try:
        agent = create_financial_agent()
        print("✅ Agent created successfully!")
        
        # Test basic functionality
        response = agent.chat("Hello! Can you tell me what you can help me with?")
        print(f"Agent response: {response['response']}")
        
    except Exception as e:
        print(f"❌ Error creating agent: {e}")
        print("Make sure you have ANTHROPIC_API_KEY in your .env file")