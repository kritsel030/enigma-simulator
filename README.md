# Enigma simulator

This Enigma simulator is capable of emulating the Enigma I as introduced in 1930.

It supports
* B or C type reflector
* 3 rotors, of type I, II and III
* plugboard with 6 cables

Note that this program and its documentation assumes that you have a proper understanding
of the workings of an Enigma machine.

# Program design model

## Contact channels
The rotor stepping mechanism means that with every step of a rotor, the connections
between its contacts and the contacts of the components to its left and right are changed.

To model this, we envision 26 fixed contact channels between each pair of neighbouring components (keyboard, rotors, reflector)
You might view these as 26 pieces of short copper wiring, each in a fixed position.

Each of these contact channels connects with a specific contact of the component to its right,
and with a specific contact of the component to its left. 
(contacts identified by a 0-based index in the diagrams below)

With each rotor step, the contact channels remain in position, while the rotor with its contacts moves,
resulting in the contacts of a stepped rotor connecting with different contact channels
on both its right and left side.

    REFLECTOR|       |--- ROTOR 1 ---|       |--- ROTOR 2 ---|       |--- ROTOR 3 ---|    	 |KEY OR 													   
             |contact| left   right  |contact| left   right  |contact| left   right  |contact|LIGHT
     contact |channel|contact contact|channel|contact contact|channel|contact contact|channel|
    =========|=======|===============|=======|===============|=======|===============|=======|======
             |       |               |       |               |       |               |   E   |  
        4    |---E---|   0       0   |---E---|  2 -----2---->|===E==>|->3------ 3    |-------|   E
             |       |               |       |    |          |       |        |      |       |
     ---3--->|===D==>|-->4---    4   |---D---|  1 |  ---1<---|<==D===|<-2---  --2--->|======>|-> D
     |       |       |      |        |       |    |  |       |       |     |         |   C   |
     |  2    |---C---|   3  -----3-->|===C==>|->0--  |  0    |---C---|  1  |    1    |-------|   C
     |       |       |               |       |       |       |       |     |         |   B   |
     ---1<---|<==B===|<--2----   2   |---B---|  4    |  4    |---B---|  0  |----0<---|<======|<- B
             |       |       |       |       |       |       |       |               |   A   |
        0    |---A---|   1   ----1<--|<==A===|<-3-----  3    |---A---|  4       4    |-------|   A


The benefit of this 'contact channel' model is that the implementation of a rotor step becomes easier. 
When a rotor steps, the program does not need to determine how the contacts of the stepped rotor 
get re-aligned with the contacts of its neighbour on the left and right, 
while at the same time taking into account the rotor position and ring setting of itself and
neighbouring rotors.

Instead, a step of a rotor only results in re-aligning its contacts with the fixed contact channels.

## Internal representation of channels, contacts, positions, etc.
Items such as keys, channels, contacts, rotor positions etc. can be represented by either characters ('A' through 'Z')
or numbers (1 through 26, or 0 through 25).

The public API of the Enigma uses the 'A' through 'Z' representation for things like input and output
and defining a rotor position. It also uses '01' through '26' for the ring setting of a rotor.

Internally however, all of these items are represented by identifiers ranging from 0 through 25.
Using this numeric representation makes it easier to calculate the effects of rotor positions and 
ring settings and to implement the wiring of a specific rotor type. 


## Single rotor mechanics: rotor position and ring setting

The examples below illustrate how the rotor position and ring setting mechanism work,
and how it translates to our program model

***************************************
### Example A - base position plus base ring setting

_rotor specification (it's the same for every example)_

- rotor wiring: (table on the left: wiring defined in terms of characters; table on the right: wiring defined in terms on contact identifiers)

```
contact on the right side:  ABCDE     01234
                            |||||     |||||
contact on the left side:   ECDBA     42310
```
- turnover position for this rotor: C (indicated by ^ in the diagram)

_rotor settings_

- position: A ==> A on outer ring is visible within | |; | | is a fixed location aligned with contact channel A

- ring setting: 01 ==> A (first character) on outer ring is aligned with first contact (contact 0); indicated by A=0

With these rotor specification and settings, this single rotor will encode a C (right contact channel) into a D (left contact channel)


```
 left   | outer     left   right   |  right                              left   | outer     left   right   |  right               
contact | ring   contact   contact | contact                            contact | ring   contact   contact | contact             
channel |position                  | channel                            channel |position                  | channel             
========|==========================|==========                          ========|==========================|==========           
        |                          |                                            |                          |                     
---E----|  (E)      4         4    |---E---                             ---E----| (A=0)     0         0    |---E---              
        |                          |             when this rotor                |                          |                     
<==D====|  (D)      3 <---    3    |----D--      advances 1 step  ==>   ---D----| (E)       4         4    |---D---              
        |                |         |             this results in                |                          |                     
---C----| (^C)      2    ---- 2    |<==C===                             ---C----| (D)       3         3    |---C---        
        |                          |                                            |                          |                     
---B----|  (B)      1         1    |---B---                             ---B----|(^C)       2         2    |---B---              
        |                          |                                            |                          |                     
---A----|  |A=0|    0         0    |---A---                             ---A----| |B|       1         1    |---A---
```

***************************************
### Example B - other position plus base ring setting
_rotor specification (it's the same for every example)_

- rotor wiring: (table on the left: wiring defined in terms of characters; table on the right: wiring defined in terms on contact identifiers)
  
```
contact on the right side:  ABCDE     01234
                            |||||     |||||
contact on the left side:   ECDBA     42310
```

- turnover position for this rotor: C (indicated by ^ in the diagram)

_rotor settings_

- position: B ==> B on outer ring is visible within | |; | | is a fixed location aligned with contact channel A

- ring setting: 01 ==> A (first character) on outer ring is aligned with first contact (contact 0); indicated by A=0

With these rotor specification and settings, this single rotor will encode a C (right contact channel) into an A (left contact channel)

```
 left   | outer     left   right   |  right    
contact | ring   contact   contact | contact   
channel |position                  | channel   
========|==========================|==========
        |                          |           
---E----| (A=0)     0         0    |---E---    
        |                          |           
---D----| (E)       4         4    |---D---    
        |                          |           
---C----| (D)       3    /--- 3    |<==C===    
        |                |         |           
---B----|(^C)       2    |    2    |---B---    
        |  _             |         |           
<==A====| |B|       1 <--/    1    |---A---
```

***************************************
### Example C - other position plus other ring setting
_rotor specification (it's the same for every example)_

- rotor wiring: (table on the left: wiring defined in terms of characters; table on the right: wiring defined in terms on contact identifiers)
  
```
contact on the right side:  ABCDE     01234
                            |||||     ||||| 
contact on the left side:   ECDBA     42310
```

- turnover position for this rotor: C (indicated by ^ in the diagram)

_rotor settings_

- position: B ==> B on outer ring is visible within | |; | | is a fixed location aligned with contact channel A

- ring setting: 04 ==> D on outer ring (base ring setting is 1; 04-01=3; A+3=D) is aligned with first contact (contact 0); indicated by D=0

With these rotor specification and settings, this single rotor will encode a C (right contact channel) to a B (left contact channel)

```
 left   | outer     left   right   |  right                             left   | outer     left   right   |  right   
contact | ring   contact   contact | contact                           contact | ring   contact   contact | contact  
channel |position                  | channel                           channel |position                  | channel  
========|==========================|=========                         ========|==========================|==========
        |                          |            when this rotor                |                          |          
---E----| (A)       2         2    |---E---     advances 1 step,       ---E----| (B)       3         3    |---E---   
        |                          |            this results in  ==>           |                          |          
---D----| (E)       1         1    |---D---                            ---D----| (A)       0         0    |---D---   
        |                          |            (the new rotor                 |                          |          
---C----| (D=0)     0    /--- 0    |<==C===     position is ^C;        ---C----| (E)       4         4    |---C---   
        |                |         |            the ^ indicates that           |                          |          
<==B===-|(^C)       4 <--/    4    |---B---     the rotor to its left  ---B----| (D=0)     3         3    |---B---   
        |                          |            will have stepped              |                          |          
---A----| |B|       3         3    |---A---     as well)               ---A----| |^C|      2         2    |---A---
```

# Information consulted
* https://en.wikipedia.org/wiki/Enigma_machine 
  basics, stepping & turnover
* https://en.wikipedia.org/wiki/Enigma_rotor_details 
  rotor and reflector specifications
* http://users.telenet.be/d.rijmenants/nl/enigmatech.htm
  ring setting (in dutch)
* http://people.physik.hu-berlin.de/~palloks/js/enigma/enigma-u_v26_en.html 
  online simulator used to verify the results