"""
Test script for the Financial Analysis Agent
Tests basic functionality without API key
"""

import os
import sys
from unittest.mock import patch

def test_imports():
    """Test if all imports work correctly"""
    print("Testing imports...")
    
    try:
        from chart_tools import ChartGenerator, create_chart_tools
        print("[OK] Chart tools imported")
    except Exception as e:
        print(f"[FAIL] Chart tools: {e}")
        return False
    
    try:
        from financial_agent import FinancialAnalysisAgent
        print("[OK] Financial agent imported")
    except Exception as e:
        print(f"[FAIL] Financial agent: {e}")
        return False
    
    try:
        from api_bridge import FinancialAnalysisBridge
        print("[OK] API bridge imported")
    except Exception as e:
        print(f"[FAIL] API bridge: {e}")
        return False
    
    return True

def test_chart_generator():
    """Test chart generator without data"""
    print("\nTesting chart generator...")
    
    try:
        from chart_tools import ChartGenerator
        generator = ChartGenerator()
        
        # Test data info without data
        result = generator.get_data_info("test")
        if "No data loaded" in result:
            print("[OK] Chart generator handles no data correctly")
            return True
        else:
            print("[FAIL] Chart generator should handle no data")
            return False
            
    except Exception as e:
        print(f"[FAIL] Chart generator test: {e}")
        return False

def test_bridge():
    """Test API bridge initialization"""
    print("\nTesting API bridge...")
    
    try:
        from api_bridge import FinancialAnalysisBridge
        bridge = FinancialAnalysisBridge()
        
        # Test data summary without agent
        result = bridge.get_data_summary()
        if result["status"] == "error" and "not initialized" in result["message"]:
            print("[OK] Bridge handles uninitialized agent correctly")
            return True
        else:
            print("[FAIL] Bridge should handle uninitialized agent")
            return False
            
    except Exception as e:
        print(f"[FAIL] Bridge test: {e}")
        return False

def main():
    print("="*50)
    print("FINANCIAL ANALYSIS AGENT - TEST SUITE")
    print("="*50)
    
    tests_passed = 0
    total_tests = 3
    
    if test_imports():
        tests_passed += 1
    
    if test_chart_generator():
        tests_passed += 1
    
    if test_bridge():
        tests_passed += 1
    
    print("\n" + "="*50)
    print(f"RESULTS: {tests_passed}/{total_tests} tests passed")
    
    if tests_passed == total_tests:
        print("✅ All tests passed! Agent is ready for use.")
        print("\nNext steps:")
        print("1. Add your ANTHROPIC_API_KEY to .env file")
        print("2. Test with: py financial_agent.py")
        print("3. Create Kotlin interface")
    else:
        print("❌ Some tests failed. Check the errors above.")
    
    print("="*50)

if __name__ == "__main__":
    main()