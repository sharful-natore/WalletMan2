#!/bin/bash
awk '
BEGIN {
    count = 0
}
{
    line1 = $0
    if (line1 == "            }") {
        getline line2
        if (line2 == "        }") {
            getline line3
            if (line3 == "    }") {
                getline line4
                if (line4 == "}") {
                    count++
                    print "    }"
                    print "}"
                    next
                } else {
                    print line1
                    print line2
                    print line3
                    print line4
                    next
                }
            } else {
                print line1
                print line2
                print line3
                next
            }
        } else {
            print line1
            print line2
            next
        }
    } else {
        print line1
    }
}
END {
    print "Replaced " count " occurrences." > "/dev/stderr"
}
' app/src/main/java/com/example/ui/screens/SanchayApp.kt > app/src/main/java/com/example/ui/screens/SanchayApp.kt.tmp
