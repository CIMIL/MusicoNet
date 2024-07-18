import mysql.connector
import uuid
import random
from pprint import pprint

mydb = mysql.connector.connect(
  host="204.216.223.231",
  user="squinkis",
  password="",
  database="musico"
)

# list of famous musicians
musicians_70s = [
    {"name": "Freddie Mercury", "description": "Lead vocalist of Queen, known for his powerful voice and flamboyant stage presence."},
    {"name": "Brian May", "description": "Guitarist of Queen, known for his distinctive sound and contributions to rock music."},
    {"name": "Roger Taylor", "description": "Drummer of Queen, also contributed vocals and songwriting."},
    {"name": "John Deacon", "description": "Bassist of Queen, known for writing some of the band's biggest hits."},

    {"name": "Robert Plant", "description": "Lead vocalist of Led Zeppelin, known for his powerful voice and charismatic performances."},
    {"name": "Jimmy Page", "description": "Guitarist of Led Zeppelin, renowned for his innovative guitar riffs and production techniques."},
    {"name": "John Paul Jones", "description": "Bassist and keyboardist of Led Zeppelin, known for his versatile musicianship."},
    {"name": "John Bonham", "description": "Drummer of Led Zeppelin, famous for his powerful drumming style."},

    {"name": "David Gilmour", "description": "Guitarist and vocalist of Pink Floyd, known for his soulful guitar solos."},
    {"name": "Roger Waters", "description": "Bassist and primary lyricist of Pink Floyd, known for his conceptual album themes."},
    {"name": "Richard Wright", "description": "Keyboardist of Pink Floyd, known for his atmospheric soundscapes."},
    {"name": "Nick Mason", "description": "Drummer of Pink Floyd, known for his steady and creative drumming."},

    {"name": "Mick Jagger", "description": "Lead vocalist of The Rolling Stones, known for his energetic stage presence and distinctive voice."},
    {"name": "Keith Richards", "description": "Guitarist of The Rolling Stones, known for his iconic riffs and songwriting."},
    {"name": "Charlie Watts", "description": "Drummer of The Rolling Stones, known for his solid and unflashy drumming style."},
    {"name": "Ronnie Wood", "description": "Guitarist of The Rolling Stones, joined the band in the mid-70s."},

    {"name": "Steven Tyler", "description": "Lead vocalist of Aerosmith, known for his wide vocal range and on-stage antics."},
    {"name": "Joe Perry", "description": "Guitarist of Aerosmith, known for his bluesy guitar playing."},
    {"name": "Tom Hamilton", "description": "Bassist of Aerosmith, known for his steady and melodic bass lines."},
    {"name": "Brad Whitford", "description": "Guitarist of Aerosmith, known for his rhythm guitar work."},
    {"name": "Joey Kramer", "description": "Drummer of Aerosmith, known for his powerful drumming style."},

    {"name": "Paul McCartney", "description": "Bassist and vocalist of The Beatles, also a prolific songwriter."},
    {"name": "John Lennon", "description": "Vocalist and rhythm guitarist of The Beatles, known for his introspective and socially conscious songs."},
    {"name": "George Harrison", "description": "Lead guitarist of The Beatles, known for his melodic guitar work and incorporation of Indian music."},
    {"name": "Ringo Starr", "description": "Drummer of The Beatles, known for his distinctive drumming style and charming personality."},

    {"name": "Stevie Nicks", "description": "Vocalist of Fleetwood Mac, known for her distinctive voice and mystical stage persona."},
    {"name": "Lindsey Buckingham", "description": "Guitarist and vocalist of Fleetwood Mac, known for his intricate guitar work and production skills."},
    {"name": "Christine McVie", "description": "Keyboardist and vocalist of Fleetwood Mac, known for her soulful voice and songwriting."},
    {"name": "John McVie", "description": "Bassist of Fleetwood Mac, known for his solid and melodic bass lines."},
    {"name": "Mick Fleetwood", "description": "Drummer and founding member of Fleetwood Mac, known for his dynamic drumming and stage presence."}
]


mycursor = mydb.cursor()
# insert into the database
def insert_mult_user(users):
    sql = "INSERT INTO users (userId, username, description) VALUES (%s, %s, %s)"
    val = []
    for user in users:
        val.append((gen_test_userId(),user['name'], user['description']))
    mycursor.executemany(sql, val)
    mydb.commit()
    print(mycursor.rowcount, "record inserted.")


def gen_test_userId():
    return "test_" + str(uuid.uuid4())

def gen_ttl_user():
    mycursor.execute("SELECT userId , username FROM users")
    users = mycursor.fetchall()
    #get test users
    test_users = [user for user in users if "test_" in user[0]]
    #Get genres
    mycursor.execute("SELECT genre_id, genre_name FROM genre")
    genres = mycursor.fetchall()
    #Get Instruments
    mycursor.execute("SELECT instrument_id, instrument_name FROM instruments")
    instruments = mycursor.fetchall()

    ttl_row = []
    for user in test_users:
        ttl_row.append("<http://www.semanticweb.org/jaco/ontologies/2023/7/musinco/Users/" + user[0] + "> a musico:HumanMusician .")
        ttl_row.append("<http://www.semanticweb.org/jaco/ontologies/2023/7/musinco/Users/" + user[0] + "> musico:plays_genre <http://www.semanticweb.org/jaco/ontologies/2023/7/musinco/Genre/"+random.choice(genres)[0]+">.")
        ttl_row.append("<http://www.semanticweb.org/jaco/ontologies/2023/7/musinco/Users/" + user[0] +
                        "> musico:plays_instrument <http://www.semanticweb.org/jaco/ontologies/2023/7/musinco/Instruments/"+
                        str(random.choice(instruments)[0])+">.")


    return ttl_row

def gen_ttl_mus_participation():
    # get all the users
    mycursor.execute("SELECT userId , username FROM users")
    users = mycursor.fetchall()
    #get test users
    test_users = [user for user in users if "test_" in user[0]]
    ttl_row = []
    for user in test_users:
        ttl_row.append("<http://www.semanticweb.org/jaco/ontologies/2023/7/musinco/Users/" + user[0] + "> musico:in_participation <ex:SavedTracks_"+user[0]+">.")
        ttl_row.append("<ex:SavedTracks_"+user[0]+"> musico:played_musical_work <ex:ProfileTracks_test_"+str(random.randint(0,20))+">.")
    return ttl_row

def gen_MWorks():
    # get all genres
    mycursor.execute("SELECT genre_id, genre_name FROM genre")
    genres = mycursor.fetchall()
    pprint(genres)
    mwork_rows = []
    for num in range(0,20):
        genre = random.choice(genres)
        bpm = random.randint(110, 140)
        mwork_rows.append("<ex:ProfileTracks_test_"+str(num)+">"+" mo:genre <http://www.semanticweb.org/jaco/ontologies/2023/7/musinco/Genre/"+genre[0]+">.")
        mwork_rows.append("<ex:ProfileTracks_test_"+str(num)+">"+" mo:bpm "+str(bpm)+".")
    
    return mwork_rows

def gen_ttl_genres():
    # get all genres
    mycursor.execute("SELECT genre_id, genre_name FROM genre")
    genres = mycursor.fetchall()
    ttl_row = []
    for genre in genres:
        ttl_row.append("<http://www.semanticweb.org/jaco/ontologies/2023/7/musinco/Genre/"+genre[0]+"> a mo:Genre .")
        ttl_row.append("<http://www.semanticweb.org/jaco/ontologies/2023/7/musinco/Genre/"+genre[0]+"> schema:name \""+genre[1]+"\" .")
    return ttl_row

def gen_ttl_instruments():
    # get all genres
    mycursor.execute("SELECT instrument_id, instrument_name FROM instruments")
    instruments = mycursor.fetchall()
    ttl_row = []
    for instrument in instruments:
        ttl_row.append("<http://www.semanticweb.org/jaco/ontologies/2023/7/musinco/Instruments/"+str(instrument[0])+"> a mo:Instrument .")
        ttl_row.append("<http://www.semanticweb.org/jaco/ontologies/2023/7/musinco/Instruments/"+str(instrument[0])+"> schema:name \""+instrument[1]+"\" .")
    return ttl_row



data = [
    *gen_ttl_genres(),
    *gen_ttl_instruments(),
    *gen_ttl_user()
]
pprint(data)

ttl_file = open("users.ttl", "a")
for line in data:
    ttl_file.write(line + "\n")
ttl_file.close()
