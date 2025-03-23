from fastapi import FastAPI

from app.routers.transactions_router import router as transactions

app = FastAPI()

app.include_router(transactions, prefix="/api/v1")
