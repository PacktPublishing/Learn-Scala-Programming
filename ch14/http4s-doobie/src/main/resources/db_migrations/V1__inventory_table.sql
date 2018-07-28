CREATE TABLE article (
  name  VARCHAR PRIMARY KEY,
  count INTEGER NOT NULL CHECK (count >= 0)
);
