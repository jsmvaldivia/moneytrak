from typing import Any, List, Dict, Optional


class DBClient:
    async def execute(self, query: str, params: tuple = ()) -> None:
        raise NotImplementedError

    async def fetch_all(self, query: str, params: tuple = ()) -> List[Dict[str, Any]]:
        raise NotImplementedError

    async def fetch_one(
        self, query: str, params: tuple = ()
    ) -> Optional[Dict[str, Any]]:
        raise NotImplementedError
