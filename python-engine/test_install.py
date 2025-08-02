#!/usr/bin/env python3
"""
Test script to verify all installations work correctly
"""

def test_imports():
    print("Testing imports...")
    
    try:
        import langchain
        print(f"[OK] LangChain: {langchain.__version__}")
    except Exception as e:
        print(f"[FAIL] LangChain: {e}")
    
    try:
        import openai
        print(f"[OK] OpenAI: {openai.__version__}")
    except Exception as e:
        print(f"[FAIL] OpenAI: {e}")
    
    try:
        import PyPDF2
        print("[OK] PyPDF2: Ready")
    except Exception as e:
        print(f"[FAIL] PyPDF2: {e}")
    
    try:
        import pdfplumber
        print("[OK] PDFPlumber: Ready")
    except Exception as e:
        print(f"[FAIL] PDFPlumber: {e}")
    
    try:
        import json
        import os
        print("[OK] Core modules: Ready")
    except Exception as e:
        print(f"[FAIL] Core modules: {e}")

if __name__ == "__main__":
    print("Python Environment Test")
    print("=" * 30)
    test_imports()
    print("=" * 30)
    print("Ready to build financial analyzer!")