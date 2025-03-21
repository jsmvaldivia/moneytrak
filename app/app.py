from fastapi import FastAPI

from .routers import transactions

app = FastAPI()

app.include_router(transactions.router)
