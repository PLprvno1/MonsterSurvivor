"""Compatibility entry point for the current Task 02.1 combat and map checks.
Task 02 numeric balance expectations are historical; current checks write only task02_1 reports.
"""
from pathlib import Path
import runpy
import sys

runner=Path(__file__).resolve().with_name('run_task02_1_checks.py')
sys.argv=[str(runner),'final']
runpy.run_path(str(runner),run_name='__main__')
