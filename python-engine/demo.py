"""
Demo script for Financial Analysis Agent
Shows how to use the agent with sample data
"""

import os
import pandas as pd
from financial_agent import create_financial_agent

def create_sample_data():
    """Create sample financial data for demonstration"""
    
    # Create sample CSV data
    data = {
        'Month': ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
        'Revenue': [50000, 55000, 48000, 62000, 58000, 65000],
        'Expenses': [30000, 32000, 28000, 35000, 33000, 38000],
        'Profit': [20000, 23000, 20000, 27000, 25000, 27000],
        'Category': ['Sales', 'Sales', 'Marketing', 'Sales', 'Marketing', 'Sales']
    }
    
    df = pd.DataFrame(data)
    
    # Save to input directory
    input_dir = "../shared-data/input"
    os.makedirs(input_dir, exist_ok=True)
    
    sample_file = os.path.join(input_dir, "sample_financial_data.csv")
    df.to_csv(sample_file, index=False)
    
    return sample_file

def demo_without_api():
    """Demo functionality that doesn't require API key"""
    print("Financial Analysis Agent - Demo (No API)")
    print("="*50)
    
    # Create sample data
    sample_file = create_sample_data()
    print(f"Sample data created: {sample_file}")
    
    # Test chart tools directly
    from chart_tools import ChartGenerator
    
    generator = ChartGenerator()
    success = generator.load_data(sample_file)
    
    if success:
        print("Data loaded successfully!")
        
        # Generate a sample chart
        result = generator.generate_bar_chart("Show revenue by month")
        print(f"Chart generation result: {result}")
        
        # Get data info
        info = generator.get_data_info("What's in this data?")
        print("Data info:", info[:200] + "...")
    
    print("\nDemo completed! Check shared-data/output/ for generated charts.")

def demo_with_api():
    """Demo with API key (if available)"""
    api_key = os.getenv("ANTHROPIC_API_KEY")
    
    if not api_key:
        print("ANTHROPIC_API_KEY not found. Please set it to test full functionality.")
        return
    
    print("Financial Analysis Agent - Full Demo")
    print("="*50)
    
    try:
        # Create agent
        agent = create_financial_agent()
        print("Agent initialized successfully!")
        
        # Load sample data
        sample_file = create_sample_data()
        result = agent.load_data(sample_file)
        print(f"Data load result: {result}")
        
        # Test chat functionality
        response = agent.chat("Create a bar chart showing revenue by month")
        print(f"Agent response: {response}")
        
        # Test analysis
        response = agent.chat("What insights can you provide about this financial data?")
        print(f"Analysis: {response['response'][:200]}...")
        
    except Exception as e:
        print(f"Error in full demo: {e}")

if __name__ == "__main__":
    print("Choose demo mode:")
    print("1. Without API (basic functionality)")
    print("2. With API (full agent functionality)")
    
    choice = input("Enter choice (1 or 2): ").strip()
    
    if choice == "1":
        demo_without_api()
    elif choice == "2":
        demo_with_api()
    else:
        print("Invalid choice. Running basic demo...")
        demo_without_api()