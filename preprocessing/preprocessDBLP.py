input = open(r"lda.docToTop.txt")
count = 1
line = input.readline()
author_set = set()
while line:
    tokens = line.split()
    authors = tokens[0].split('/')
    author_set.add(authors[0])
    author_set.add(authors[1])
    line = input.readline()
    count += 1
input.close()
author_list = sorted(author_set)
print("There are {} authors".format(len(author_list)))

output_authors = open(r"DBLP-authors.csv", 'w')
output_authors.write("name")
for author in author_list:
    output_authors.write("\n" + author)
output_authors.close()

output_authortopic = open(r"DBLP-author-topic-relationships.csv", "w")
output_authortopic.write("author,topic,probability")
input = open(r"lda.docToTop.txt")
line = input.readline()
while line:
    tokens = line.split()
    authors = tokens[0].split('/')
    for i in range(2,len(tokens)):
        topic_prob = tokens[i][1:-1].split(',')
        output_authortopic.write("\n" + authors[0] + "," + topic_prob[0] + "," + topic_prob[1])
        output_authortopic.write("\n" + authors[1] + "," + topic_prob[0] + "," + topic_prob[1])
    line = input.readline()
output_authortopic.close()
