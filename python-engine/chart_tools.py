"""
Chart generation tools for LangChain Agent
Generates various types of charts based on natural language prompts
"""

import os
import json
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import plotly.express as px
import plotly.graph_objects as go
from datetime import datetime
from typing import Dict, Any, Optional
from langchain.tools import Tool

# Set style for better looking charts
plt.style.use('seaborn-v0_8')
sns.set_palette("husl")

class ChartGenerator:
    def __init__(self, output_dir: str = "../shared-data/output"):
        self.output_dir = output_dir
        self.current_data = None
        self.data_context = {}
        
        # Ensure output directory exists
        os.makedirs(output_dir, exist_ok=True)
    
    def load_data(self, file_path: str) -> bool:
        """Load data from CSV or Excel file"""
        try:
            if file_path.endswith('.csv'):
                self.current_data = pd.read_csv(file_path)
            elif file_path.endswith(('.xlsx', '.xls')):
                self.current_data = pd.read_excel(file_path)
            else:
                return False
            
            # Store data context for the agent
            self.data_context = {
                "columns": list(self.current_data.columns),
                "shape": self.current_data.shape,
                "dtypes": self.current_data.dtypes.to_dict(),
                "sample": self.current_data.head(3).to_dict()
            }
            return True
        except Exception as e:
            print(f"Error loading data: {e}")
            return False
    
    def generate_bar_chart(self, prompt: str) -> str:
        """Generate bar chart based on prompt"""
        if self.current_data is None:
            return "No data loaded. Please load a CSV or Excel file first."
        
        try:
            # Simple logic to determine x and y columns
            # In production, you'd use Claude to parse the prompt more intelligently
            numeric_cols = self.current_data.select_dtypes(include=['number']).columns
            categorical_cols = self.current_data.select_dtypes(include=['object']).columns
            
            if len(numeric_cols) == 0 or len(categorical_cols) == 0:
                return "Data doesn't have suitable columns for bar chart"
            
            x_col = categorical_cols[0]
            y_col = numeric_cols[0]
            
            # Create the chart
            plt.figure(figsize=(12, 8))
            data_grouped = self.current_data.groupby(x_col)[y_col].sum().sort_values(ascending=False)
            
            bars = plt.bar(data_grouped.index, data_grouped.values)
            plt.title(f'{y_col} by {x_col}', fontsize=16, fontweight='bold')
            plt.xlabel(x_col, fontsize=12)
            plt.ylabel(y_col, fontsize=12)
            plt.xticks(rotation=45)
            
            # Add value labels on bars
            for bar in bars:
                height = bar.get_height()
                plt.text(bar.get_x() + bar.get_width()/2., height,
                        f'{height:.1f}', ha='center', va='bottom')
            
            plt.tight_layout()
            
            # Save chart
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            filename = f"bar_chart_{timestamp}.png"
            filepath = os.path.join(self.output_dir, filename)
            plt.savefig(filepath, dpi=300, bbox_inches='tight')
            plt.close()
            
            return f"Bar chart generated successfully: {filename}"
            
        except Exception as e:
            return f"Error generating bar chart: {str(e)}"
    
    def generate_line_chart(self, prompt: str) -> str:
        """Generate line chart for time series or trends"""
        if self.current_data is None:
            return "No data loaded. Please load a CSV or Excel file first."
        
        try:
            # Look for date columns and numeric columns
            date_cols = []
            for col in self.current_data.columns:
                if 'date' in col.lower() or 'time' in col.lower():
                    date_cols.append(col)
            
            numeric_cols = self.current_data.select_dtypes(include=['number']).columns
            
            if len(date_cols) == 0 or len(numeric_cols) == 0:
                return "Data doesn't have suitable date and numeric columns for line chart"
            
            date_col = date_cols[0]
            y_col = numeric_cols[0]
            
            # Convert date column
            self.current_data[date_col] = pd.to_datetime(self.current_data[date_col])
            data_sorted = self.current_data.sort_values(date_col)
            
            plt.figure(figsize=(12, 8))
            plt.plot(data_sorted[date_col], data_sorted[y_col], marker='o', linewidth=2, markersize=6)
            plt.title(f'{y_col} Over Time', fontsize=16, fontweight='bold')
            plt.xlabel(date_col, fontsize=12)
            plt.ylabel(y_col, fontsize=12)
            plt.xticks(rotation=45)
            plt.grid(True, alpha=0.3)
            
            plt.tight_layout()
            
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            filename = f"line_chart_{timestamp}.png"
            filepath = os.path.join(self.output_dir, filename)
            plt.savefig(filepath, dpi=300, bbox_inches='tight')
            plt.close()
            
            return f"Line chart generated successfully: {filename}"
            
        except Exception as e:
            return f"Error generating line chart: {str(e)}"
    
    def generate_pie_chart(self, prompt: str) -> str:
        """Generate pie chart for categorical data distribution"""
        if self.current_data is None:
            return "No data loaded. Please load a CSV or Excel file first."
        
        try:
            categorical_cols = self.current_data.select_dtypes(include=['object']).columns
            
            if len(categorical_cols) == 0:
                return "Data doesn't have categorical columns for pie chart"
            
            col = categorical_cols[0]
            value_counts = self.current_data[col].value_counts()
            
            plt.figure(figsize=(10, 10))
            colors = plt.cm.Set3(range(len(value_counts)))
            
            wedges, texts, autotexts = plt.pie(value_counts.values, 
                                             labels=value_counts.index,
                                             autopct='%1.1f%%',
                                             colors=colors,
                                             startangle=90)
            
            plt.title(f'Distribution of {col}', fontsize=16, fontweight='bold')
            
            # Make percentage text bold
            for autotext in autotexts:
                autotext.set_color('white')
                autotext.set_fontweight('bold')
            
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            filename = f"pie_chart_{timestamp}.png"
            filepath = os.path.join(self.output_dir, filename)
            plt.savefig(filepath, dpi=300, bbox_inches='tight')
            plt.close()
            
            return f"Pie chart generated successfully: {filename}"
            
        except Exception as e:
            return f"Error generating pie chart: {str(e)}"
    
    def generate_heatmap(self, prompt: str) -> str:
        """Generate correlation heatmap"""
        if self.current_data is None:
            return "No data loaded. Please load a CSV or Excel file first."
        
        try:
            numeric_data = self.current_data.select_dtypes(include=['number'])
            
            if numeric_data.shape[1] < 2:
                return "Need at least 2 numeric columns for correlation heatmap"
            
            correlation_matrix = numeric_data.corr()
            
            plt.figure(figsize=(12, 10))
            sns.heatmap(correlation_matrix, 
                       annot=True, 
                       cmap='coolwarm', 
                       center=0,
                       square=True,
                       fmt='.2f')
            plt.title('Correlation Heatmap', fontsize=16, fontweight='bold')
            plt.tight_layout()
            
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            filename = f"heatmap_{timestamp}.png"
            filepath = os.path.join(self.output_dir, filename)
            plt.savefig(filepath, dpi=300, bbox_inches='tight')
            plt.close()
            
            return f"Heatmap generated successfully: {filename}"
            
        except Exception as e:
            return f"Error generating heatmap: {str(e)}"
    
    def get_data_info(self, prompt: str) -> str:
        """Get information about the loaded data"""
        if self.current_data is None:
            return "No data loaded. Please load a CSV or Excel file first."
        
        info = {
            "shape": self.current_data.shape,
            "columns": list(self.current_data.columns),
            "numeric_columns": list(self.current_data.select_dtypes(include=['number']).columns),
            "categorical_columns": list(self.current_data.select_dtypes(include=['object']).columns),
            "missing_values": self.current_data.isnull().sum().to_dict(),
            "sample_data": self.current_data.head(3).to_dict()
        }
        
        return f"Data Information:\n{json.dumps(info, indent=2, default=str)}"

def create_chart_tools(chart_generator: ChartGenerator) -> list:
    """Create LangChain tools for chart generation"""
    
    tools = [
        Tool(
            name="generate_bar_chart",
            description="Generate a bar chart to show categorical data with numeric values. Use for comparisons between categories.",
            func=chart_generator.generate_bar_chart
        ),
        Tool(
            name="generate_line_chart", 
            description="Generate a line chart to show trends over time or continuous data. Use for time series analysis.",
            func=chart_generator.generate_line_chart
        ),
        Tool(
            name="generate_pie_chart",
            description="Generate a pie chart to show percentage distribution of categorical data. Use for showing parts of a whole.",
            func=chart_generator.generate_pie_chart
        ),
        Tool(
            name="generate_heatmap",
            description="Generate a correlation heatmap to show relationships between numeric variables.",
            func=chart_generator.generate_heatmap
        ),
        Tool(
            name="get_data_info",
            description="Get detailed information about the loaded dataset including columns, data types, and sample data.",
            func=chart_generator.get_data_info
        )
    ]
    
    return tools