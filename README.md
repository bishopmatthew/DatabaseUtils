# DatabaseUtils

A really simple ORM for Android that uses reflection to persist classes that extends SqlObject. Any public, non-transient, non-static fields of type int, long, boolean, enum, float, String, or Date be saved to the database. 

# Usage

## Create tables
To use, create the database as normal with [SQLiteOpenHelper](http://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html). You can create the tables for your object with `SqlObject.createTable();` [See this example in my Hacker News repo.](https://github.com/bishopmatthew/HackerNews/blob/master/src/com/airlocksoftware/hackernews/cache/DbHelperSingleton.java)

## Extending SqlObject
You'll want to add a `create(SQLiteDatabase db, yourArgs…)` to do setup & error checking, and then call `super.createWithId()` or `super.createAndGenerateId()` to save it to the database. `update()`, `read()`, and `delete()` all work like you would expect them to.

[See this example for an example of how to set up your SqlObjects.](https://github.com/bishopmatthew/HackerNews/blob/master/src/com/airlocksoftware/hackernews/model/Story.java)

## Querying
You use the regular db.query() method to get a cursor to the data. Then you can go through each row and use `SqlObject.readFromCursor()` to read them. [Look here for an example of reading rows from the database.](https://github.com/bishopmatthew/HackerNews/blob/master/src/com/airlocksoftware/hackernews/model/Story.java#L140)

# License
Released under the MIT License.

Copyright (c) 2013 Matthew Bishop

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
