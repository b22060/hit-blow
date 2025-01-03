CREATE TABLE users (
    userid IDENTITY PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE matches(
  matchid IDENTITY PRIMARY KEY,
  userid1 INT NOT NULL,
  userid2 INT NOT NULL,
  usernum1 VARCHAR(4) NOT NULL,
  usernum2 VARCHAR(4) NOT NULL,
  judge VARCHAR NOT NULL,
  isActive BOOLEAN NOT NULL,
  FOREIGN KEY (userid1) REFERENCES users(userid),
  FOREIGN KEY (userid2) REFERENCES users(userid)
);

CREATE TABLE matchinfo (
  matchinfoid IDENTITY PRIMARY KEY,
  matchid INT NOT NULL,
  userid INT NOT NULL,
  guess  VARCHAR(4) NOT NULL,
  hit INT NOT NULL,
  blow INT NOT NULL,
  isActive BOOLEAN NOT NULL,
  FOREIGN KEY (userid) REFERENCES users(userid),
  FOREIGN KEY (matchid) REFERENCES matches(matchid)
);
