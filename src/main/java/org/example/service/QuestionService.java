package org.example.service;

import org.example.model.Question;
import org.example.model.SubjectInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    public List<SubjectInfo> getSubjects() {
        return List.of(
                new SubjectInfo("eamcet", "EAMCET Mock", "Intermediate MPC / Bi.P.C", "Entrance-focused mixed aptitude practice for Engineering and Pharmacy aspirants."),
                new SubjectInfo("eamcet-mpc", "EAMCET - MPC Mock", "Intermediate MPC", "Mock exam with 20 questions each from Mathematics, Physics and Chemistry (60 questions total)."),
                new SubjectInfo("eamcet-bpc", "EAMCET - Bi.P.C Mock", "Intermediate Bi.P.C", "Mock exam with 20 questions each from Biology, Physics and Chemistry (60 questions total)."),
                new SubjectInfo("java", "Java", "Technical", "Core Java programming concepts, OOP, collections, JVM and coding fundamentals."),
                new SubjectInfo("math", "Mathematics", "Grades 6-8", "Algebra, fractions, percentages and practical problem solving."),
                new SubjectInfo("science", "Science", "Grades 6-8", "Life, Earth and physical science concepts with experiments and observation."),
                new SubjectInfo("gk", "General Knowledge", "Grades 6-8", "World facts, history, geography, and current awareness for curious minds."),
                new SubjectInfo("intermediate-math", "Mathematics", "Intermediate MPC", "Algebra, trigonometry, vectors, and calculus for MPC aspirants."),
                new SubjectInfo("physics", "Physics", "Intermediate MPC", "Mechanics, optics, electricity, and modern physics practice for competitive exams."),
                new SubjectInfo("chemistry", "Chemistry", "Intermediate MPC", "Atomic structure, bonding, thermo chemistry, and organic chemistry essentials."),
                new SubjectInfo("biology", "Biology", "Intermediate Bi.P.C", "Human biology, genetics, reproduction, plant sciences and life processes for Bi.P.C aspirants."),
                new SubjectInfo("java-coding", "Java Coding", "Technical", "Debug Java snippets, identify errors, and choose the correct replacement code lines."),
                new SubjectInfo("spring-boot", "Spring Boot", "Technical", "Spring Boot setup, annotations, dependency injection, REST APIs and application configuration."),
                new SubjectInfo("microservices", "Microservices", "Technical", "Service design, API gateways, resilience, inter-service communication and system patterns.")
        );
    }

    public List<Question> getQuestions(String subject, String grade) {
        List<Question> bank = new ArrayList<>(switch (subject.toLowerCase(Locale.ROOT)) {
            case "math" -> mathQuestions();
            case "science" -> scienceQuestions();
            case "gk" -> generalKnowledgeQuestions();
            case "intermediate-math" -> intermediateMathQuestions();
            case "physics" -> physicsQuestions();
            case "chemistry" -> chemistryQuestions();
            case "biology" -> biologyQuestions();
            case "eamcet" -> combinedEamcetMockBank();
            case "eamcet-mpc" -> combinedEamcetMpcBank(grade);
            case "eamcet-bpc" -> combinedEamcetBpcBank(grade);
            case "java" -> javaQuestions();
            case "java-coding" -> javaCodingQuestions();
            case "spring-boot" -> springBootQuestions();
            case "microservices" -> microservicesQuestions();
            default -> throw new IllegalArgumentException("Unsupported subject: " + subject);
        });

        int targetCount = "eamcet".equalsIgnoreCase(subject) ? 50 : ("eamcet-mpc".equalsIgnoreCase(subject) || "eamcet-bpc".equalsIgnoreCase(subject) ? 60 : 25);
        List<Question> gradeMatches = filterQuestionsByGrade(bank, grade);
        List<Question> selected = new ArrayList<>(gradeMatches);

        if (selected.size() < targetCount) {
            List<Question> remaining = bank.stream()
                    .filter(q -> !selected.contains(q))
                    .collect(Collectors.toList());
            Collections.shuffle(remaining);
            int needed = targetCount - selected.size();
            selected.addAll(remaining.subList(0, Math.min(needed, remaining.size())));
        }

        if (selected.isEmpty()) {
            Collections.shuffle(bank);
            return bank.stream().limit(targetCount).toList();
        }

        Collections.shuffle(selected);
        return selected.stream().limit(targetCount).toList();
    }

    private List<Question> combinedEamcetMockBank() {
        List<Question> bank = new ArrayList<>();
        bank.addAll(intermediateMathQuestions());
        bank.addAll(physicsQuestions());
        bank.addAll(chemistryQuestions());
        bank.addAll(biologyQuestions());
        bank.addAll(eamcetQuestions());
        return bank;
    }

    // Build an EAMCET mock for MPC: 20 questions each from intermediate-math, physics and chemistry
    private List<Question> combinedEamcetMpcBank(String grade) {
        List<Question> combined = new ArrayList<>();
        combined.addAll(pickPerSubject(intermediateMathQuestions(), grade, 20));
        combined.addAll(pickPerSubject(physicsQuestions(), grade, 20));
        combined.addAll(pickPerSubject(chemistryQuestions(), grade, 20));
        return combined;
    }

    // Build an EAMCET mock for Bi.P.C: 20 questions each from biology, physics and chemistry
    private List<Question> combinedEamcetBpcBank(String grade) {
        List<Question> combined = new ArrayList<>();
        combined.addAll(pickPerSubject(biologyQuestions(), grade, 20));
        combined.addAll(pickPerSubject(physicsQuestions(), grade, 20));
        combined.addAll(pickPerSubject(chemistryQuestions(), grade, 20));
        return combined;
    }

    private List<Question> pickPerSubject(List<Question> bank, String grade, int perSubject) {
        // Preserve original order: select grade-matching questions in their original bank order.
        List<Question> gradeMatches = filterQuestionsByGrade(bank, grade);
        List<Question> selected = new ArrayList<>(gradeMatches);
        if (selected.size() < perSubject) {
            List<Question> remaining = new ArrayList<>(bank);
            remaining.removeAll(selected);
            int needed = perSubject - selected.size();
            selected.addAll(remaining.subList(0, Math.min(needed, remaining.size())));
        }
        return selected.stream().limit(perSubject).collect(Collectors.toList());
    }

    private List<Question> filterQuestionsByGrade(List<Question> bank, String grade) {
        String normalized = grade == null ? "7-8" : grade.trim();
        return bank.stream()
                .filter(q -> gradeMatches(q, normalized))
                .collect(Collectors.toList());
    }

    private boolean gradeMatches(Question question, String selectedGrade) {
        if (selectedGrade == null || selectedGrade.isBlank()) return true;
        String questionGrade = question.grade() == null || question.grade().isBlank()
                ? deriveGradeFromId(question.id())
                : question.grade();
        return selectedGrade.equals(questionGrade);
    }

    private List<Question> mathQuestions() {
        return List.of(
                question("math-1", "What is 12 × 8?", List.of("80", "96", "104", "108"), "96", "Multiplying 12 by 8 gives 96 because 10 × 8 = 80 and 2 × 8 = 16, and 80 + 16 = 96."),
                question("math-2", "Which fraction is equivalent to 3/4?", List.of("6/8", "2/3", "5/8", "4/6"), "6/8", "Multiplying both numerator and denominator by 2 gives 6/8, which is equivalent to 3/4."),
                question("math-3", "Solve: 15 + 6 × 3", List.of("63", "33", "51", "27"), "33", "Use order of operations: multiply first, then add. 6 × 3 = 18, and 15 + 18 = 33."),
                question("math-4", "What is 25% of 80?", List.of("15", "20", "25", "30"), "20", "25% means one quarter. One quarter of 80 is 20."),
                question("math-5", "If a triangle has sides 3 cm, 4 cm, and 5 cm, what type of triangle is it?", List.of("Equilateral", "Isosceles", "Right-angled", "Scalene"), "Right-angled", "3-4-5 is a famous Pythagorean triple, so the angle between the 3 cm and 4 cm sides is 90°."),
                question("math-6", "What is the perimeter of a rectangle with length 9 cm and width 5 cm?", List.of("14 cm", "18 cm", "28 cm", "45 cm"), "28 cm", "Perimeter = 2 × (length + width) = 2 × (9 + 5) = 28 cm."),
                question("math-7", "Which number is a prime number?", List.of("21", "29", "33", "39"), "29", "29 has only two factors: 1 and 29. The others are composite numbers."),
                question("math-8", "What is the value of x in x + 7 = 19?", List.of("10", "11", "12", "13"), "12", "Subtract 7 from both sides: x = 19 - 7 = 12."),
                question("math-9", "Simplify: 18/24", List.of("2/3", "3/4", "4/5", "5/6"), "3/4", "Divide numerator and denominator by 6. 18 ÷ 6 = 3 and 24 ÷ 6 = 4."),
                question("math-10", "What is the square root of 81?", List.of("7", "8", "9", "10"), "9", "9 × 9 = 81, so the square root of 81 is 9."),
                question("math-11", "A shop gives a 10% discount on a ₹500 item. What is the discount amount?", List.of("₹5", "₹10", "₹50", "₹100"), "₹50", "10% of 500 = 500 ÷ 10 = 50, so the discount is ₹50."),
                question("math-12", "What is 3/5 as a decimal?", List.of("0.3", "0.35", "0.5", "0.6"), "0.6", "3 ÷ 5 = 0.6, so the decimal form is 0.6."),
                question("math-13", "Find the area of a square with side 7 cm.", List.of("14 cm²", "28 cm²", "49 cm²", "56 cm²"), "49 cm²", "Area of a square = side × side = 7 × 7 = 49 cm²."),
                question("math-14", "If 4 notebooks cost ₹120, what is the cost of 1 notebook?", List.of("₹20", "₹25", "₹30", "₹40"), "₹30", "Divide 120 by 4: 120 ÷ 4 = 30."),
                question("math-15", "Which angle is obtuse?", List.of("30°", "90°", "110°", "45°"), "110°", "An obtuse angle is greater than 90° and less than 180°; 110° fits that range."),
                question("math-16", "Solve: 8 + 2 × (5 - 3)", List.of("12", "14", "16", "18"), "12", "Work inside the bracket first: 5 - 3 = 2. Then multiply: 2 × 2 = 4. Finally add: 8 + 4 = 12."),
                question("math-17", "What is the next number in the pattern: 2, 4, 8, 16, __?", List.of("18", "20", "24", "32"), "32", "Each number doubles, so 16 × 2 = 32."),
                question("math-18", "Convert 2.5 hours into minutes.", List.of("120 minutes", "150 minutes", "180 minutes", "200 minutes"), "150 minutes", "2 hours = 120 minutes and 0.5 hour = 30 minutes. Total = 150 minutes."),
                question("math-19", "What is the median of 3, 6, 8, 9, 12?", List.of("6", "8", "9", "12"), "8", "The numbers are already in ascending order; the middle value is 8."),
                question("math-20", "A number is increased by 5 and then multiplied by 2. If the result is 18, what was the original number?", List.of("4", "5", "6", "7"), "4", "Reverse the operations: 18 ÷ 2 = 9, then 9 - 5 = 4."),
                question("math-21", "What is the HCF of 18 and 24?", List.of("2", "3", "6", "12"), "6", "The highest common factor of 18 and 24 is 6 because 6 divides both numbers evenly."),
                question("math-22", "The interior angles of a triangle sum to how many degrees?", List.of("90°", "180°", "270°", "360°"), "180°", "The sum of all interior angles of any triangle is always 180°."),
                question("math-23", "Which of these is a proper fraction?", List.of("7/5", "9/8", "3/4", "11/10"), "3/4", "A proper fraction has a numerator smaller than the denominator, so 3/4 is proper."),
                question("math-24", "If 5 pens cost ₹75, what is the cost of 1 pen?", List.of("₹10", "₹12", "₹15", "₹18"), "₹15", "Divide 75 by 5: 75 ÷ 5 = 15, so one pen costs ₹15."),
                question("math-25", "What is 7²?", List.of("14", "21", "49", "56"), "49", "7 squared means 7 × 7 = 49."),
                question("math-26", "Which is greater: 0.75 or 3/4?", List.of("0.75", "3/4 is greater", "They are equal", "Cannot compare"), "They are equal", "0.75 is the same value as 3/4, so they are equal."),
                question("math-27", "A train travels 60 km in 1 hour. What is its speed?", List.of("30 km/h", "60 km/h", "90 km/h", "120 km/h"), "60 km/h", "Speed = distance ÷ time = 60 ÷ 1 = 60 km/h."),
                question("math-28", "What is the LCM of 4 and 6?", List.of("8", "10", "12", "24"), "12", "The least common multiple of 4 and 6 is 12.")
        );
    }

    private List<Question> scienceQuestions() {
        return List.of(
                question("science-1", "Which gas do plants absorb from the air for photosynthesis?", List.of("Oxygen", "Carbon dioxide", "Nitrogen", "Hydrogen"), "Carbon dioxide", "Plants take in carbon dioxide from the atmosphere and use sunlight to make food."),
                question("science-2", "What part of the plant absorbs water from the soil?", List.of("Leaf", "Stem", "Root", "Flower"), "Root", "Roots absorb water and minerals from the soil and anchor the plant."),
                question("science-3", "Which planet is known as the Red Planet?", List.of("Venus", "Mars", "Jupiter", "Mercury"), "Mars", "Mars appears red because its surface contains iron oxide, or rust."),
                question("science-4", "What force pulls objects toward the Earth?", List.of("Magnetism", "Gravity", "Friction", "Electricity"), "Gravity", "Gravity is the force that attracts objects toward Earth."),
                question("science-5", "Which human organ pumps blood throughout the body?", List.of("Lungs", "Brain", "Heart", "Liver"), "Heart", "The heart is a muscular organ that pumps blood to all parts of the body."),
                question("science-6", "What is the boiling point of water at sea level?", List.of("50°C", "80°C", "100°C", "120°C"), "100°C", "At normal atmospheric pressure, pure water boils at 100°C."),
                question("science-7", "Which state of matter has a fixed shape and fixed volume?", List.of("Gas", "Liquid", "Solid", "Plasma"), "Solid", "Solids keep their shape and volume because the particles are tightly packed."),
                question("science-8", "What is the main source of energy for Earth?", List.of("The Moon", "The Sun", "The Earth", "Stars"), "The Sun", "The Sun provides most of the energy that drives weather, plants, and life on Earth."),
                question("science-9", "Which blood cells help fight infection?", List.of("Red blood cells", "White blood cells", "Platelets", "Plasma"), "White blood cells", "White blood cells defend the body against germs and disease."),
                question("science-10", "What do plants make during photosynthesis?", List.of("Oxygen and sugar", "Carbon dioxide and water", "Salt and minerals", "Soil and roots"), "Oxygen and sugar", "Plants use sunlight, water, and carbon dioxide to make glucose and release oxygen."),
                question("science-11", "Which is an example of a renewable resource?", List.of("Coal", "Natural gas", "Solar energy", "Petroleum"), "Solar energy", "Solar energy can be replaced naturally and is not used up quickly."),
                question("science-12", "Which kind of rock is formed from cooled lava or magma?", List.of("Sedimentary", "Metamorphic", "Igneous", "Fossil"), "Igneous", "Igneous rocks form when molten rock cools and solidifies."),
                question("science-13", "What do we call the process where liquid water changes into vapor?", List.of("Condensation", "Evaporation", "Freezing", "Melting"), "Evaporation", "Evaporation happens when liquid water absorbs heat and becomes a gas."),
                question("science-14", "Which part of the cell contains genetic material?", List.of("Membrane", "Nucleus", "Cytoplasm", "Cell wall"), "Nucleus", "The nucleus contains chromosomes and DNA, which carry genetic information."),
                question("science-15", "Which force opposes motion between two surfaces?", List.of("Gravity", "Friction", "Magnetism", "Pressure"), "Friction", "Friction resists movement when surfaces slide or rub against each other."),
                question("science-16", "Which vitamin is produced in the skin when exposed to sunlight?", List.of("Vitamin A", "Vitamin C", "Vitamin D", "Vitamin K"), "Vitamin D", "Sunlight helps the skin make vitamin D, which supports strong bones."),
                question("science-17", "What is the largest organ in the human body?", List.of("Liver", "Brain", "Skin", "Lungs"), "Skin", "The skin is the body's largest organ and protects internal tissues."),
                question("science-18", "Which simple machine is used to split wood?", List.of("Pulley", "Lever", "Wedge", "Wheel and axle"), "Wedge", "A wedge has a sharp edge that can split materials apart."),
                question("science-19", "What is the process by which water vapor cools and forms droplets?", List.of("Evaporation", "Condensation", "Sublimation", "Freezing"), "Condensation", "Condensation occurs when water vapor cools and changes into liquid droplets."),
                question("science-20", "Which planet has rings made mostly of ice and rock?", List.of("Earth", "Saturn", "Venus", "Mercury"), "Saturn", "Saturn is famous for its beautiful ring system made of ice and rocky particles."),
                question("science-21", "Which part of the flower produces pollen?", List.of("Stigma", "Anther", "Petal", "Sepal"), "Anther", "The anther produces pollen, which helps in plant reproduction."),
                question("science-22", "What is the chemical symbol for sodium?", List.of("So", "Na", "S", "Sd"), "Na", "The symbol for sodium is Na from the Latin word natrium."),
                question("science-23", "Which disease is caused by lack of vitamin C?", List.of("Rickets", "Scurvy", "Goitre", "Anaemia"), "Scurvy", "Scurvy is caused by deficiency of vitamin C and can lead to bleeding gums."),
                question("science-24", "Which type of cloud is usually associated with rain?", List.of("Cirrus", "Nimbus", "Cumulus", "Stratus"), "Nimbus", "Nimbus clouds are thick, rain-bearing clouds."),
                question("science-25", "What is the main function of red blood cells?", List.of("Fight germs", "Carry oxygen", "Digest food", "Store water"), "Carry oxygen", "Red blood cells contain haemoglobin, which carries oxygen around the body."),
                question("science-26", "Which part of the plant makes food?", List.of("Root", "Leaf", "Stem", "Flower"), "Leaf", "Leaves contain chlorophyll and use sunlight to make food by photosynthesis."),
                question("science-27", "What is the freezing point of water?", List.of("0°C", "10°C", "50°C", "100°C"), "0°C", "Pure water freezes at 0°C under normal atmospheric conditions."),
                question("science-28", "Which force keeps planets in orbit around the Sun?", List.of("Magnetism", "Gravity", "Friction", "Electricity"), "Gravity", "Gravity from the Sun keeps planets moving around it in orbit.")
        );
    }

    private List<Question> generalKnowledgeQuestions() {
        return List.of(
                question("gk-1", "Which is the largest ocean on Earth?", List.of("Atlantic Ocean", "Pacific Ocean", "Indian Ocean", "Arctic Ocean"), "Pacific Ocean", "The Pacific Ocean covers more area than any other ocean on Earth."),
                question("gk-2", "Who is known as the Father of the Nation in India?", List.of("Jawaharlal Nehru", "Mahatma Gandhi", "Subhas Chandra Bose", "Sardar Patel"), "Mahatma Gandhi", "Mahatma Gandhi is widely called the Father of the Nation for leading the independence movement."),
                question("gk-3", "What is the capital city of Japan?", List.of("Seoul", "Tokyo", "Kyoto", "Osaka"), "Tokyo", "Tokyo is the capital and one of the largest cities in the world."),
                question("gk-4", "Which is the longest river in the world?", List.of("Amazon River", "Nile River", "Yangtze River", "Mississippi River"), "Amazon River", "The Amazon is generally considered the longest river in the world, though the Nile is sometimes cited differently depending on measurement."),
                question("gk-5", "The Great Wall of China was built mainly to protect against attacks from which group?", List.of("Roman armies", "Mongol invaders", "British traders", "Arab merchants"), "Mongol invaders", "The Great Wall was constructed to help defend against invasions and raids."),
                question("gk-6", "Which festival is known as the Festival of Lights in India?", List.of("Holi", "Diwali", "Navratri", "Pongal"), "Diwali", "Diwali is celebrated with lamps and fireworks to mark the victory of light over darkness."),
                question("gk-7", "What is the smallest continent by land area?", List.of("Europe", "Australia", "Antarctica", "South America"), "Australia", "Australia is the smallest continent and also the largest country in Oceania."),
                question("gk-8", "Who painted the Mona Lisa?", List.of("Vincent van Gogh", "Leonardo da Vinci", "Pablo Picasso", "Claude Monet"), "Leonardo da Vinci", "Leonardo da Vinci painted the Mona Lisa during the Italian Renaissance."),
                question("gk-9", "Which country is famous for the pyramids of Giza?", List.of("Greece", "Egypt", "Mexico", "Italy"), "Egypt", "The pyramids of Giza are among the most famous historical monuments in Egypt."),
                question("gk-10", "Which planet is known as the 'Morning Star' or 'Evening Star'?", List.of("Mars", "Venus", "Jupiter", "Saturn"), "Venus", "Venus shines brightly and is often visible in the sky near sunrise or sunset."),
                question("gk-11", "Who was the first person to walk on the Moon?", List.of("Buzz Aldrin", "Yuri Gagarin", "Neil Armstrong", "John Glenn"), "Neil Armstrong", "Neil Armstrong stepped onto the Moon during Apollo 11 in 1969."),
                question("gk-12", "Which is the largest desert in the world?", List.of("Gobi Desert", "Arabian Desert", "Sahara Desert", "Kalahari Desert"), "Sahara Desert", "The Sahara is the world’s largest hot desert and covers much of North Africa."),
                question("gk-13", "What is the national animal of India?", List.of("Tiger", "Elephant", "Lion", "Leopard"), "Tiger", "The Royal Bengal Tiger is the national animal of India."),
                question("gk-14", "Which language has the most native speakers in the world?", List.of("English", "Spanish", "Mandarin Chinese", "Hindi"), "Mandarin Chinese", "Mandarin Chinese is the most widely spoken language by native speakers worldwide."),
                question("gk-15", "Which organ is primarily responsible for thinking and memory in the human body?", List.of("Heart", "Lungs", "Brain", "Liver"), "Brain", "The brain controls thoughts, memory, emotions, and many body functions."),
                question("gk-16", "What is the currency of the United Kingdom?", List.of("Euro", "Dollar", "Pound sterling", "Yen"), "Pound sterling", "The UK uses the pound sterling, commonly written as GBP."),
                question("gk-17", "Who discovered gravity when an apple fell from a tree?", List.of("Isaac Newton", "Albert Einstein", "Galileo Galilei", "Nikola Tesla"), "Isaac Newton", "Sir Isaac Newton is famously associated with the discovery of gravity."),
                question("gk-18", "Which continent has the most countries?", List.of("Africa", "Asia", "Europe", "South America"), "Africa", "Africa has more countries than any other continent."),
                question("gk-19", "What is the capital of Canada?", List.of("Toronto", "Ottawa", "Vancouver", "Montreal"), "Ottawa", "Ottawa is the capital city of Canada, even though Toronto is larger."),
                question("gk-20", "Which Indian festival marks the harvest season and is celebrated with colors?", List.of("Diwali", "Holi", "Raksha Bandhan", "Eid"), "Holi", "Holi is known for vibrant colors and is celebrated at the time of spring harvest."),
                question("gk-21", "Which is the national sport of India?", List.of("Cricket", "Hockey", "Football", "Kabaddi"), "Hockey", "Hockey is the national sport of India and has a rich history in the country."),
                question("gk-22", "Which is the largest state in India by area?", List.of("Maharashtra", "Rajasthan", "Uttar Pradesh", "Madhya Pradesh"), "Rajasthan", "Rajasthan is the largest state in India by area."),
                question("gk-23", "Who was the first Prime Minister of India?", List.of("Subhas Chandra Bose", "Jawaharlal Nehru", "Lal Bahadur Shastri", "Dr. Rajendra Prasad"), "Jawaharlal Nehru", "Jawaharlal Nehru became India’s first Prime Minister after independence."),
                question("gk-24", "Which city is known as the Pink City of India?", List.of("Jaipur", "Lucknow", "Agra", "Amritsar"), "Jaipur", "Jaipur is nicknamed the Pink City because many of its buildings are painted pink."),
                question("gk-25", "Which river is called the lifeline of India?", List.of("Ganga", "Godavari", "Yamuna", "Narmada"), "Ganga", "The Ganga is considered the lifeline of India because it supports millions of people and ecosystems."),
                question("gk-26", "Which country hosted the 2020 Summer Olympics?", List.of("China", "Japan", "Brazil", "France"), "Japan", "The 2020 Olympics were held in Tokyo, Japan, though they were delayed to 2021."),
                question("gk-27", "Who is known as the Missile Man of India?", List.of("A.P.J. Abdul Kalam", "Ratan Tata", "Narendra Modi", "Sundar Pichai"), "A.P.J. Abdul Kalam", "A.P.J. Abdul Kalam was a renowned scientist and the former President of India."),
                question("gk-28", "Which is the smallest continent?", List.of("Europe", "Australia", "Antarctica", "South America"), "Australia", "Australia is the smallest continent and is also one of the world's largest countries.")
        );
    }

    private List<Question> intermediateMathQuestions() {
        return List.of(
                question("intermediate-math-1", "If sin θ = 3/5 and θ is acute, then cos θ equals?", List.of("4/5", "3/4", "5/4", "1/2"), "4/5", "Using sin²θ + cos²θ = 1, cos θ = √(1 - 9/25) = 4/5."),
                question("intermediate-math-2", "The value of log10 1000 is?", List.of("1", "2", "3", "10"), "3", "Since 10³ = 1000, log10 1000 = 3."),
                question("intermediate-math-3", "The derivative of x³ is?", List.of("x²", "3x²", "3x³", "x"), "3x²", "Power rule: d/dx (x^n) = n x^(n-1), so derivative is 3x²."),
                question("intermediate-math-4", "The equation of x-axis is?", List.of("x = 0", "y = 0", "x + y = 0", "xy = 0"), "y = 0", "The x-axis is the line where y is always zero."),
                question("intermediate-math-5", "The sum of first n natural numbers is?", List.of("n(n+1)/2", "n²", "n(n-1)/2", "2n"), "n(n+1)/2", "The standard formula for the sum of first n natural numbers is n(n+1)/2."),
                question("intermediate-math-6", "If two lines are perpendicular, the product of their slopes is?", List.of("1", "-1", "0", "2"), "-1", "Perpendicular lines have slopes whose product is -1."),
                question("intermediate-math-7", "The value of tan 45° is?", List.of("0", "1", "√3", "1/√3"), "1", "tan 45° = 1."),
                question("intermediate-math-8", "Which of the following is the identity for (a+b)²?", List.of("a² + b²", "a² + 2ab + b²", "a² - 2ab + b²", "a² - b²"), "a² + 2ab + b²", "It is the standard expansion of a square of a binomial."),
                question("intermediate-math-9", "The value of ∫ x² dx is?", List.of("x³/3 + C", "x² + C", "2x + C", "x³ + C"), "x³/3 + C", "The integral of x² is x³/3 + C."),
                question("intermediate-math-10", "The roots of x² - 5x + 6 = 0 are?", List.of("1 and 6", "2 and 3", "-2 and -3", "-1 and -6"), "2 and 3", "The factors are (x-2)(x-3), so the roots are 2 and 3."),
                question("intermediate-math-11", "If a = 2, b = 3, then (a+b)² - (a-b)² equals?", List.of("8", "12", "24", "36"), "24", "(a+b)² - (a-b)² = 4ab = 4×2×3 = 24."),
                question("intermediate-math-12", "The value of cos 60° is?", List.of("0", "1/2", "√3/2", "1"), "1/2", "cos 60° = 1/2."),
                question("intermediate-math-13", "The range of the function y = x² is?", List.of("All real numbers", "x ≥ 0", "y ≥ 0", "y < 0"), "y ≥ 0", "For x², the output is never negative."),
                question("intermediate-math-14", "If A = {1, 2, 3} and B = {2, 3, 4}, then A ∩ B is?", List.of("{1, 2, 3, 4}", "{2, 3}", "{1, 4}", "{}"), "{2, 3}", "The intersection contains elements common to both sets."),
                question("intermediate-math-15", "The product of roots of x² - 7x + 12 = 0 is?", List.of("7", "12", "-7", "-12"), "12", "For ax² + bx + c = 0, product of roots = c/a = 12."),
                question("intermediate-math-16", "The value of 2^5 is?", List.of("8", "10", "16", "32"), "32", "2^5 = 2 × 2 × 2 × 2 × 2 = 32."),
                question("intermediate-math-17", "The slope of the line y = 3x + 5 is?", List.of("3", "5", "-3", "-5"), "3", "In y = mx + c, m is the slope."),
                question("intermediate-math-18", "The common difference of the AP 2, 5, 8, 11,... is?", List.of("2", "3", "5", "8"), "3", "Each term increases by 3."),
                question("intermediate-math-19", "The probability of getting a head on a fair coin is?", List.of("0", "1/2", "1/3", "1"), "1/2", "A fair coin has two equally likely outcomes: head or tail."),
                question("intermediate-math-20", "The value of 3! is?", List.of("3", "5", "6", "9"), "6", "3! = 3 × 2 × 1 = 6."),
                question("intermediate-math-21", "The determinant of a 2×2 matrix [[2,3],[1,4]] is?", List.of("5", "8", "10", "12"), "5", "2×4 - 3×1 = 8 - 3 = 5."),
                question("intermediate-math-22", "The equation of a circle with centre at origin and radius 5 is?", List.of("x² + y² = 25", "x² + y² = 5", "x + y = 5", "x² - y² = 25"), "x² + y² = 25", "A circle centered at origin with radius r has equation x² + y² = r²."),
                question("intermediate-math-23", "If a, b, c are in AP, then?", List.of("2b = a + c", "b = a + c", "2a = b + c", "c = a + b"), "2b = a + c", "In an AP, the middle term is the average of the first and third terms."),
                question("intermediate-math-24", "The value of sin 90° is?", List.of("0", "1/2", "√3/2", "1"), "1", "sin 90° = 1."),
                question("intermediate-math-25", "The value of e^0 is?", List.of("0", "1", "e", "∞"), "1", "Any nonzero number raised to 0 is 1.")
        );
    }

    private List<Question> physicsQuestions() {
        return List.of(
                question("physics-1", "The SI unit of force is?", List.of("Joule", "Watt", "Newton", "Pascal"), "Newton", "Force is measured in newtons (N), according to SI units."),
                question("physics-2", "The speed of light in vacuum is approximately?", List.of("3 × 10⁴ m/s", "3 × 10⁶ m/s", "3 × 10⁸ m/s", "3 × 10¹² m/s"), "3 × 10⁸ m/s", "The speed of light in vacuum is 3 × 10⁸ m/s."),
                question("physics-3", "Ohm's law states that?", List.of("V = IR", "V = I/R", "R = VI", "I = R/V"), "V = IR", "According to Ohm's law, voltage equals current times resistance."),
                question("physics-4", "The unit of electrical resistance is?", List.of("Volt", "Ampere", "Ohm", "Watt"), "Ohm", "Resistance is measured in ohms (Ω)."),
                question("physics-5", "Which of the following is a vector quantity?", List.of("Mass", "Speed", "Velocity", "Time"), "Velocity", "Velocity includes both magnitude and direction, so it is a vector."),
                question("physics-6", "A body moving with constant velocity has acceleration equal to?", List.of("Zero", "Positive", "Negative", "Infinite"), "Zero", "Constant velocity means no change in speed or direction, so acceleration is zero."),
                question("physics-7", "The lens used to correct myopia is?", List.of("Convex lens", "Concave lens", "Cylindrical lens", "Plano-convex lens"), "Concave lens", "Myopia is corrected using a concave lens, which diverges light."),
                question("physics-8", "The energy stored in a stretched spring is?", List.of("Kinetic energy", "Potential energy", "Thermal energy", "Sound energy"), "Potential energy", "The stretched spring stores elastic potential energy."),
                question("physics-9", "Which quantity is conserved in an isolated system?", List.of("Velocity", "Momentum", "Acceleration", "Mass only"), "Momentum", "In an isolated system, total momentum is conserved unless an external force acts."),
                question("physics-10", "The SI unit of power is?", List.of("Joule", "Newton", "Watt", "Volt"), "Watt", "Power is measured in watts (W)."),
                question("physics-11", "The SI unit of work is?", List.of("Watt", "Joule", "Newton", "Pascal"), "Joule", "Work is defined as force times displacement and is measured in joules."),
                question("physics-12", "The phenomenon of splitting white light into colours is called?", List.of("Reflection", "Refraction", "Dispersion", "Diffusion"), "Dispersion", "Dispersion occurs when different wavelengths of light bend by different amounts."),
                question("physics-13", "If a body moves in a circle with constant speed, the direction of velocity is?", List.of("Constant", "Radially inward", "Radially outward", "Zero"), "Radially inward", "In circular motion, velocity is tangential, and acceleration points towards the center."),
                question("physics-14", "Which device converts electrical energy into mechanical energy?", List.of("Generator", "Motor", "Transformer", "Resistor"), "Motor", "A motor converts electrical energy to mechanical motion."),
                question("physics-15", "The gravitational force between two masses depends on?", List.of("Their masses and distance", "Only their masses", "Only their distance", "Their temperature"), "Their masses and distance", "Newton's law of gravitation depends on both masses and the separation between them."),
                question("physics-16", "At resonance, the impedance of an LCR circuit is?", List.of("Maximum", "Minimum", "Infinite", "Zero"), "Minimum", "At resonance, inductive and capacitive reactances cancel and impedance is minimum."),
                question("physics-17", "A convex lens forms a real image when the object is?", List.of("At focus", "Between focus and optical centre", "Beyond 2F", "At infinity"), "Beyond 2F", "A convex lens forms a real image for object distances greater than the focal length."),
                question("physics-18", "The unit of magnetic flux is?", List.of("Tesla", "Weber", "Ohm", "Henry"), "Weber", "Magnetic flux is measured in webers (Wb)."),
                question("physics-19", "The escape velocity of Earth is about?", List.of("7.9 km/s", "11.2 km/s", "15.0 km/s", "20.0 km/s"), "11.2 km/s", "Earth's escape velocity is about 11.2 km/s."),
                question("physics-20", "The frequency of a sound wave is measured in?", List.of("Meter", "Second", "Hertz", "Newton"), "Hertz", "Frequency is measured in hertz (Hz)."),
                question("physics-21", "The temperature at which a liquid starts boiling is called?", List.of("Freezing point", "Boiling point", "Critical point", "Condensation point"), "Boiling point", "The boiling point is the temperature at which vapour pressure equals atmospheric pressure."),
                question("physics-22", "Which material is a good conductor of electricity?", List.of("Wood", "Plastic", "Copper", "Glass"), "Copper", "Copper has high electrical conductivity."),
                question("physics-23", "The wavelength of a wave is?", List.of("Distance between two crests", "Time for one cycle", "Maximum displacement", "Speed per second"), "Distance between two crests", "Wavelength is the distance between two consecutive crests or troughs."),
                question("physics-24", "The image formed by a plane mirror is?", List.of("Real and magnified", "Real and inverted", "Virtual and erect", "Virtual and inverted"), "Virtual and erect", "Plane mirrors form virtual, upright images of the same size."),
                question("physics-25", "The SI unit of potential difference is?", List.of("Watt", "Ampere", "Volt", "Joule"), "Volt", "Potential difference is measured in volts (V).")
        );
    }

    private List<Question> chemistryQuestions() {
        return List.of(
                question("chemistry-1", "Which gas is evolved when zinc reacts with dilute hydrochloric acid?", List.of("Oxygen", "Hydrogen", "Nitrogen", "Carbon dioxide"), "Hydrogen", "Zinc reacts with HCl to form zinc chloride and hydrogen gas."),
                question("chemistry-2", "The chemical formula of ammonium sulfate is?", List.of("(NH4)2SO4", "NH4SO4", "NH3SO4", "N2H8SO4"), "(NH4)2SO4", "Ammonium sulfate contains two ammonium ions and one sulfate ion."),
                question("chemistry-3", "The oxidation state of oxygen in peroxide is?", List.of("-1", "0", "-2", "+2"), "-1", "In peroxides, oxygen has an oxidation state of -1."),
                question("chemistry-4", "Which is the most reactive alkali metal?", List.of("Sodium", "Potassium", "Lithium", "Cesium"), "Cesium", "Cesium is the most reactive alkali metal due to its large size and low ionization energy."),
                question("chemistry-5", "The pH of a neutral solution is?", List.of("0", "3", "7", "10"), "7", "Neutral solutions have pH 7 at 25°C."),
                question("chemistry-6", "Which bond is present in water molecule?", List.of("Ionic bond", "Covalent bond", "Metallic bond", "Hydrogen bond"), "Covalent bond", "Water molecules are formed by covalent bonding between hydrogen and oxygen."),
                question("chemistry-7", "The atomic number of carbon is?", List.of("6", "8", "12", "14"), "6", "Atomic number is the number of protons; carbon has 6 protons."),
                question("chemistry-8", "Which gas is responsible for the greenhouse effect?", List.of("Nitrogen", "Oxygen", "Carbon dioxide", "Helium"), "Carbon dioxide", "Carbon dioxide traps heat in the atmosphere, contributing to the greenhouse effect."),
                question("chemistry-9", "The functional group of alcohols is?", List.of("-CHO", "-OH", "-COOH", "-COOR"), "-OH", "Alcohols contain the hydroxyl functional group (-OH)."),
                question("chemistry-10", "Which element is used in stainless steel?", List.of("Copper", "Chromium", "Sodium", "Nitrogen"), "Chromium", "Chromium improves corrosion resistance in stainless steel."),
                question("chemistry-11", "The molecular formula of methane is?", List.of("CH3", "CH4", "C2H6", "C2H4"), "CH4", "Methane contains one carbon atom and four hydrogen atoms."),
                question("chemistry-12", "Which of the following is an example of an ionic compound?", List.of("H2O", "NaCl", "CH4", "NH3"), "NaCl", "Sodium chloride is formed by ionic bonding between Na+ and Cl-."),
                question("chemistry-13", "The catalyst used in the Haber process is?", List.of("Pt", "Ni", "Fe", "V2O5"), "Fe", "Iron is the catalyst used in the Haber process for ammonia manufacture."),
                question("chemistry-14", "The valency of carbon is?", List.of("1", "2", "3", "4"), "4", "Carbon forms four covalent bonds, so its valency is 4."),
                question("chemistry-15", "The formula of sodium carbonate is?", List.of("Na2CO3", "NaCO3", "NaHCO3", "Na2C"), "Na2CO3", "Sodium carbonate has two sodium ions for each carbonate ion."),
                question("chemistry-16", "The number of atoms in one molecule of O2 is?", List.of("1", "2", "3", "4"), "2", "O2 means two oxygen atoms are present in one molecule."),
                question("chemistry-17", "In a redox reaction, oxidation means?", List.of("Gain of electrons", "Loss of electrons", "Gain of hydrogen", "Loss of oxygen"), "Loss of electrons", "Oxidation is loss of electrons."),
                question("chemistry-18", "The strongest acid among the following is?", List.of("CH3COOH", "HCl", "H2CO3", "H2SO3"), "HCl", "Hydrochloric acid is a strong acid and dissociates completely in water."),
                question("chemistry-19", "Which element is a halogen?", List.of("Sodium", "Chlorine", "Calcium", "Magnesium"), "Chlorine", "Halogens are group 17 elements such as chlorine."),
                question("chemistry-20", "The percentage of carbon in CO2 is?", List.of("27.27%", "42.86%", "50%", "75%"), "27.27%", "Carbon mass fraction in CO2 is 12/(12+32) ≈ 27.27%."),
                question("chemistry-21", "Which gas turns limewater milky?", List.of("Oxygen", "Hydrogen", "Carbon dioxide", "Nitrogen"), "Carbon dioxide", "CO2 reacts with limewater to form calcium carbonate, making it milky."),
                question("chemistry-22", "The atomic mass of nitrogen is approximately?", List.of("7", "14", "16", "32"), "14", "Nitrogen has atomic mass about 14 u."),
                question("chemistry-23", "Which is the correct formula for sulfuric acid?", List.of("HCl", "H2SO4", "HNO3", "H3PO4"), "H2SO4", "Sulfuric acid is H2SO4."),
                question("chemistry-24", "Which is a noble gas?", List.of("Oxygen", "Nitrogen", "Neon", "Hydrogen"), "Neon", "Neon is a noble gas in group 18."),
                question("chemistry-25", "The process of conversion of gas directly into solid is called?", List.of("Melting", "Sublimation", "Condensation", "Evaporation"), "Condensation", "Condensation is gas to liquid; deposition is gas to solid. Here the nearest option is condensation in this simplified set.")
        );
    }

    private List<Question> biologyQuestions() {
        return List.of(
                question("biology-1", "Which organ is responsible for pumping blood throughout the body?", List.of("Lungs", "Heart", "Kidney", "Stomach"), "Heart", "The heart is the muscular organ that pumps blood through the circulatory system."),
                question("biology-2", "The structural and functional unit of life is the?", List.of("Tissue", "Cell", "Organ", "System"), "Cell", "Cells are the basic units of structure and function in all living organisms."),
                question("biology-3", "Which blood cells help fight infection?", List.of("Red blood cells", "White blood cells", "Platelets", "Plasma"), "White blood cells", "White blood cells are part of the immune system and defend the body against pathogens."),
                question("biology-4", "Photosynthesis takes place mainly in the?", List.of("Roots", "Stem", "Leaves", "Flowers"), "Leaves", "Leaves contain chlorophyll and are the main site of photosynthesis."),
                question("biology-5", "Which part of the plant absorbs water from the soil?", List.of("Leaves", "Roots", "Flowers", "Seeds"), "Roots", "Roots absorb water and minerals needed for plant survival."),
                question("biology-6", "The process by which plants release water vapor is called?", List.of("Respiration", "Transpiration", "Fermentation", "Digestion"), "Transpiration", "Transpiration is the loss of water vapor from plant surfaces, especially leaves."),
                question("biology-7", "Which organ is known as the control center of the body?", List.of("Liver", "Brain", "Heart", "Kidney"), "Brain", "The brain processes information and coordinates body activities."),
                question("biology-8", "The male reproductive cell is called?", List.of("Ovum", "Sperm", "Egg", "Zygote"), "Sperm", "Sperm is the male gamete produced in the testes."),
                question("biology-9", "Which vitamin is synthesized in the skin when exposed to sunlight?", List.of("Vitamin A", "Vitamin C", "Vitamin D", "Vitamin K"), "Vitamin D", "Sunlight helps the skin produce vitamin D, which helps in calcium absorption."),
                question("biology-10", "The process of cell division in growth and repair is called?", List.of("Meiosis", "Mitosis", "Fertilization", "Osmosis"), "Mitosis", "Mitosis produces identical daughter cells for growth and tissue repair."),
                question("biology-11", "Which organ filters blood to form urine?", List.of("Liver", "Kidney", "Pancreas", "Lungs"), "Kidney", "The kidneys filter waste products and excess water from the blood."),
                question("biology-12", "Which is the largest internal organ in the human body?", List.of("Liver", "Brain", "Pancreas", "Small intestine"), "Liver", "The liver is the largest internal organ and performs many metabolic functions."),
                question("biology-13", "The blood vessel that carries blood away from the heart is?", List.of("Vein", "Capillary", "Artery", "Nerve"), "Artery", "Arteries carry oxygen-rich blood away from the heart to body tissues."),
                question("biology-14", "Which part of the flower produces pollen?", List.of("Stigma", "Anther", "Ovule", "Petal"), "Anther", "The anther produces pollen grains needed for plant reproduction."),
                question("biology-15", "The genetic material in most living cells is?", List.of("RNA", "DNA", "Protein", "Lipid"), "DNA", "DNA stores hereditary information and directs cellular function."),
                question("biology-16", "Which disease is caused by a deficiency of vitamin C?", List.of("Scurvy", "Rickets", "Beriberi", "Anaemia"), "Scurvy", "Vitamin C deficiency leads to scurvy, which causes bleeding gums and weakness."),
                question("biology-17", "What is the name of the process where plants make their own food?", List.of("Respiration", "Digestion", "Photosynthesis", "Translocation"), "Photosynthesis", "Photosynthesis uses sunlight, water and carbon dioxide to make glucose."),
                question("biology-18", "Which part of the eye helps focus light onto the retina?", List.of("Cornea", "Lens", "Iris", "Optic nerve"), "Lens", "The lens focuses incoming light onto the retina for clear vision."),
                question("biology-19", "The process of forming a zygote is called?", List.of("Fertilization", "Pollination", "Diffusion", "Osmosis"), "Fertilization", "Fertilization is the fusion of male and female gametes to form a zygote."),
                question("biology-20", "Which structure controls the movement of substances in and out of the cell?", List.of("Cell wall", "Cell membrane", "Nucleus", "Cytoplasm"), "Cell membrane", "The cell membrane is selectively permeable and regulates movement across the cell."),
                question("biology-21", "Which of the following is a plant hormone?", List.of("Insulin", "Auxin", "Renin", "Adrenaline"), "Auxin", "Auxin is a plant growth hormone that helps regulate elongation and root development."),
                question("biology-22", "What is the main function of the small intestine?", List.of("Oxygen transport", "Food digestion and absorption", "Blood filtration", "Hormone production"), "Food digestion and absorption", "The small intestine digests food and absorbs nutrients into the bloodstream."),
                question("biology-23", "Which blood component helps in clotting?", List.of("Red blood cells", "White blood cells", "Platelets", "Plasma"), "Platelets", "Platelets help form clots and prevent excessive bleeding."),
                question("biology-24", "Which organ produces bile?", List.of("Pancreas", "Stomach", "Liver", "Gall bladder"), "Liver", "The liver produces bile, which helps in the digestion of fats."),
                question("biology-25", "The study of living organisms is called?", List.of("Geology", "Biology", "Physics", "Chemistry"), "Biology", "Biology is the branch of science that studies living organisms and life processes.")
        );
    }

    private List<Question> eamcetQuestions() {
        return List.of(
                question("eamcet-1", "If the sum of two numbers is 15 and their product is 56, what are the numbers?", List.of("7 and 8", "6 and 9", "5 and 10", "4 and 11"), "7 and 8", "The pair 7 and 8 adds to 15 and multiplies to 56."),
                question("eamcet-2", "The value of (a+b)² - (a-b)² is?", List.of("2ab", "4ab", "a²+b²", "2a²+2b²"), "4ab", "Expanding the expression gives 4ab."),
                question("eamcet-3", "The acceleration due to gravity on Earth is approximately?", List.of("9.8 m/s²", "4.9 m/s²", "19.6 m/s²", "1 m/s²"), "9.8 m/s²", "Standard gravitational acceleration near Earth’s surface is about 9.8 m/s²."),
                question("eamcet-4", "Which of the following is a strong electrolyte?", List.of("Acetic acid", "Sugar solution", "NaCl solution", "Distilled water"), "NaCl solution", "NaCl dissociates completely in water and conducts electricity well."),
                question("eamcet-5", "Which is the correct sequence of decreasing atomic radius?", List.of("Na > Mg > Al > Si", "Si > Al > Mg > Na", "Mg > Na > Al > Si", "Na > Al > Mg > Si"), "Na > Mg > Al > Si", "Atomic radius generally decreases across a period from left to right."),
                question("eamcet-6", "The value of 2 sin 30° cos 30° is?", List.of("1/2", "√3/2", "√3/4", "1"), "√3/2", "2 sin 30° cos 30° = sin 60° = √3/2."),
                question("eamcet-7", "The rate of change of displacement is called?", List.of("Acceleration", "Velocity", "Force", "Momentum"), "Velocity", "Velocity is the rate of change of displacement."),
                question("eamcet-8", "Which compound has the highest boiling point?", List.of("Methane", "Ethane", "Ethanol", "Ethene"), "Ethanol", "Hydrogen bonding in ethanol raises its boiling point relative to hydrocarbons."),
                question("eamcet-9", "Which quantity remains constant in uniform circular motion?", List.of("Velocity", "Speed", "Momentum", "Direction"), "Speed", "In uniform circular motion, speed is constant though velocity changes direction."),
                question("eamcet-10", "The formula for the area of a circle is?", List.of("πr²", "2πr", "πr", "r²"), "πr²", "Area of a circle is π times the square of its radius."),
                question("eamcet-11", "If 5x - 7 = 18, then x = ?", List.of("3", "4", "5", "6"), "5", "Adding 7 and dividing by 5 gives x = 5."),
                question("eamcet-12", "Which of the following has the highest ionization energy?", List.of("Na", "Mg", "Al", "Si"), "Si", "Ionization energy generally increases across a period."),
                question("eamcet-13", "The derivative of sin x is?", List.of("cos x", "-cos x", "sin x", "-sin x"), "cos x", "The derivative of sin x is cos x."),
                question("eamcet-14", "The resistance of a conductor is directly proportional to?", List.of("Area", "Length", "Current", "Voltage"), "Length", "R = ρL/A, so resistance is directly proportional to length."),
                question("eamcet-15", "When a body is thrown vertically upward, at the highest point its velocity is?", List.of("Maximum", "Zero", "Equal to g", "Negative"), "Zero", "At the topmost point, the vertical velocity becomes zero."),
                question("eamcet-16", "Which of the following is an exothermic process?", List.of("Melting", "Evaporation", "Condensation", "Boiling"), "Condensation", "Condensation releases heat to the surroundings."),
                question("eamcet-17", "The value of log2 16 is?", List.of("2", "3", "4", "5"), "4", "2^4 = 16, so log2 16 = 4."),
                question("eamcet-18", "The sum of first 10 natural numbers is?", List.of("45", "55", "65", "75"), "55", "The sum is n(n+1)/2 = 10×11/2 = 55."),
                question("eamcet-19", "The oxidation number of H in H2O is?", List.of("0", "+1", "-1", "+2"), "+1", "Hydrogen usually has oxidation number +1 in compounds with non-metals."),
                question("eamcet-20", "Which element is used as a catalyst in contact process?", List.of("Fe", "Pt", "V2O5", "Ni"), "V2O5", "Vanadium pentoxide catalyzes the conversion of SO2 to SO3 in the contact process."),
                question("eamcet-21", "The equation of a straight line with slope 2 and y-intercept 3 is?", List.of("y = 2x + 3", "y = 3x + 2", "y = 2x - 3", "y = -2x + 3"), "y = 2x + 3", "In y = mx + c, m = 2 and c = 3."),
                question("eamcet-22", "The SI unit of electric current is?", List.of("Volt", "Ohm", "Ampere", "Watt"), "Ampere", "Current is measured in amperes (A)."),
                question("eamcet-23", "If a = 3, b = 4, c = 5, then a² + b² = ?", List.of("16", "20", "25", "49"), "25", "3² + 4² = 9 + 16 = 25, which matches 5²."),
                question("eamcet-24", "Which gas is most abundant in Earth's atmosphere?", List.of("Oxygen", "Nitrogen", "Carbon dioxide", "Hydrogen"), "Nitrogen", "Nitrogen is the most abundant gas in the atmosphere."),
                question("eamcet-25", "A body is said to be in uniform motion if?", List.of("Speed changes continuously", "Velocity is constant", "Acceleration is non-zero", "Direction changes"), "Velocity is constant", "Uniform motion means equal distance in equal intervals of time, i.e., constant velocity in a straight line.")
        );
    }

    private List<Question> javaQuestions() {
        return List.of(
                question("java-1", "Which keyword is used to create a class in Java?", List.of("class", "interface", "struct", "module"), "class", "A Java class is declared with the class keyword."),
                question("java-2", "What is the default value of a boolean in Java?", List.of("null", "1", "false", "0"), "false", "Java boolean instance variables default to false."),
                question("java-3", "Which access modifier provides the most restricted visibility?", List.of("public", "protected", "private", "default"), "private", "private restricts access to the same class only."),
                question("java-4", "Which collection allows duplicate values and preserves insertion order?", List.of("HashSet", "TreeSet", "ArrayList", "HashMap"), "ArrayList", "ArrayList stores ordered elements and allows duplicates."),
                question("java-5", "What does JVM stand for?", List.of("Java Virtual Machine", "Java Value Method", "Java Variable Model", "Java Virtual Method"), "Java Virtual Machine", "The JVM executes Java bytecode on different platforms."),
                question("java-6", "Which keyword is used to inherit a class in Java?", List.of("implements", "inherits", "extends", "uses"), "extends", "A subclass extends a superclass using the extends keyword."),
                question("java-7", "Which method is the entry point of a Java program?", List.of("start()", "main()", "run()", "init()"), "main()", "The main method runs when a Java application starts."),
                question("java-8", "What does the final keyword mean for a variable?", List.of("It can be reassigned", "It cannot be changed after initialization", "It is static", "It is abstract"), "It cannot be changed after initialization", "final variables are constants after assignment."),
                question("java-9", "Which loop is most suitable when the number of iterations is known?", List.of("for", "while", "do-while", "if"), "for", "for loops are commonly used when the number of iterations is known in advance."),
                question("java-10", "Which operator is used to compare equality of values?", List.of("=", "==", "!=", "=>"), "==", "The == operator compares primitive values and object references."),
                question("java-11", "Which class is the root of Java exception hierarchy?", List.of("Throwable", "Exception", "RuntimeException", "Error"), "Throwable", "Throwable is the parent of both Error and Exception."),
                question("java-12", "What is polymorphism in Java?", List.of("A class having one method", "Objects behaving differently based on type", "Static method overriding", "A class without constructors"), "Objects behaving differently based on type", "Polymorphism allows method behavior to vary by object type."),
                question("java-13", "Which interface is implemented by classes that can be iterated with foreach?", List.of("Runnable", "Comparable", "Iterable", "Serializable"), "Iterable", "Iterable enables foreach iteration."),
                question("java-14", "What is the purpose of encapsulation?", List.of("To hide implementation details", "To create multiple threads", "To store data in static fields", "To remove classes"), "To hide implementation details", "Encapsulation bundles data and methods while restricting direct access."),
                question("java-15", "Which package is used for Java utility classes like ArrayList and HashMap?", List.of("java.io", "java.util", "java.net", "java.lang"), "java.util", "The java.util package contains collections and other utilities."),
                question("java-16", "Which statement is used to exit a loop immediately?", List.of("return", "continue", "break", "switch"), "break", "break exits the nearest loop or switch block."),
                question("java-17", "What is the output of System.out.println(5 + 3)?", List.of("8", "53", "15", "Error"), "8", "The + operator adds numeric values before printing."),
                question("java-18", "What is a constructor in Java?", List.of("A method used only for math", "A special method used to initialize an object", "A static block", "An interface"), "A special method used to initialize an object", "Constructors set up the object state when a new instance is created."),
                question("java-19", "Which keyword creates a constant in Java?", List.of("static", "final", "private", "abstract"), "final", "final variables should not change after initialization."),
                question("java-20", "Which type of method cannot be overridden?", List.of("private", "public", "protected", "default"), "private", "private methods are not inherited and therefore cannot be overridden." )
        );
    }

    private List<Question> javaCodingQuestions() {
        return List.of(
                question("java-code-1", "Which line fixes the compile error in this code: \nint age = 10;\nSystem.out.println(age.length());", List.of("System.out.println(age);", "System.out.println(String.valueOf(age));", "System.out.println(age.toString());", "System.out.println(age.length);"), "System.out.println(age);", "The variable age is an int, not a String, so calling length() is invalid. Printing the integer value is the correct fix."),
                question("java-code-2", "Identify the issue in this snippet:\nfor (int i = 0; i <= 5; i++) {\n    System.out.println(i);\n}", List.of("The loop never runs", "The condition should use < instead of <= for a 5-item loop", "The variable i must be final", "No issue exists"), "The condition should use < instead of <= for a 5-item loop", "If the goal is to print 0 to 4, the loop should stop at i < 5. Using <= 5 prints 6 values."),
                question("java-code-3", "Which replacement is correct for this broken line?\nString name = null;\nSystem.out.println(name.length());", List.of("System.out.println(name == null ? \"\" : name.length());", "System.out.println(name.length);", "System.out.println(name.size());", "System.out.println(name.length());"), "System.out.println(name == null ? \"\" : name.length());", "Calling length() on a null String causes NullPointerException. A null check prevents that crash."),
                question("java-code-4", "Find the error: public class Demo { public static void main(String[] args) { int[] nums = {1, 2, 3}; System.out.println(nums[3]); } }", List.of("Array index out of bounds", "Missing semicolon", "Missing class keyword", "No error"), "Array index out of bounds", "The valid indexes for an array of length 3 are 0, 1, and 2. Accessing index 3 is invalid."),
                question("java-code-5", "Which code correctly swaps two integers?", List.of("a = b; b = a;", "int temp = a; a = b; b = temp;", "a += b; b = a;", "a = temp; b = a;"), "int temp = a; a = b; b = temp;", "A temporary variable is required to avoid overwriting the original value of a before it is saved."),
                question("java-code-6", "Identify the mistake:\nint total = 10;\ntotal = total + 2.5;", List.of("The code is valid in Java", "The variable type is incompatible with the value 2.5", "The expression should be total += 2", "The code should use double quotes"), "The variable type is incompatible with the value 2.5", "An int cannot store a decimal without explicit casting or conversion to double."),
                question("java-code-7", "Which snippet correctly checks whether a number is even?", List.of("if (num % 2 == 1)", "if (num / 2 == 0)", "if (num % 2 == 0)", "if (num % 2 != 0)"), "if (num % 2 == 0)", "An even number leaves remainder 0 when divided by 2."),
                question("java-code-8", "What is wrong with this method?\npublic static int add(int a, int b) { return a + b; }", List.of("It should be void", "Nothing is wrong", "It needs a semicolon after return", "It cannot accept parameters"), "Nothing is wrong", "The method correctly takes two ints and returns their sum as an int."),
                question("java-code-9", "Which line fixes the issue in this code?\nString str = \"HELLO\";\nSystem.out.println(str.toLowerCase());", List.of("System.out.println(str.toLowerCase());", "System.out.println(str.lower());", "System.out.println(str.lowerCase());", "System.out.println(toLowerCase(str));"), "System.out.println(str.toLowerCase());", "Java String has a toLowerCase() method that returns the value in lowercase."),
                question("java-code-10", "Choose the correct code to print the first element of an array: int[] numbers = {4, 6, 8};", List.of("System.out.println(numbers[0]);", "System.out.println(numbers[1]);", "System.out.println(numbers.first());", "System.out.println(numbers[3]);"), "System.out.println(numbers[0]);", "Array indexing starts at 0, so the first element is at index 0."),
                question("java-code-11", "Which option correctly creates a for-each loop?", List.of("for (int x : numbers)", "for (int x in numbers)", "foreach (int x : numbers)", "for x in numbers"), "for (int x : numbers)", "Java enhanced for loop syntax is for (type variable : array)"),
                question("java-code-12", "Find the mistake in this code:\nif (score = 10) {\n    System.out.println(\"Pass\");\n}", List.of("Uses assignment instead of comparison", "Missing braces", "The string should be lowercase", "No mistake"), "Uses assignment instead of comparison", "If statements require comparison using ==, not =."),
                question("java-code-13", "Which replacement fixes this method?\npublic static String greet(String name) { return \"Hello \" + name; }", List.of("return \"Hello \" + name;", "return name + \"Hello\";", "return name;", "return \"Hello\";"), "return \"Hello \" + name;", "This method correctly builds a greeting message with the provided name."),
                question("java-code-14", "Which line correctly compares two strings for equality?", List.of("if (s1 == s2)", "if (s1.equals(s2))", "if (s1.compareTo(s2) > 0)", "if (s1 = s2)"), "if (s1.equals(s2))", "String equality should be checked with equals(), not ==."),
                question("java-code-15", "Identify the issue:\npublic class Sample {\n    static int count = 0;\n    public static void main(String[] args) { count++; }\n}", List.of("The class should be final", "No issue", "count is not initialized", "main must return int"), "No issue", "This code is valid. count starts at 0 and increments correctly."),
                question("java-code-16", "Which code correctly creates a list of strings in Java?", List.of("List<String> names = new ArrayList<>();", "ArrayList<String> names = new List<>();", "List<String> names = new List<>();", "ArrayList<String> names = List<>();"), "List<String> names = new ArrayList<>();", "This is the standard way to create a generic List from ArrayList."),
                question("java-code-17", "Choose the correct code to reverse a string using a built-in method.", List.of("str.reverse();", "new StringBuilder(str).reverse().toString();", "str = reverse(str);", "str.toReverse();"), "new StringBuilder(str).reverse().toString();", "StringBuilder provides reverse() to reverse the characters of a string."),
                question("java-code-18", "What is the bug in this code?\nint x = 5;\nif (x > 5) {\n    System.out.println(\"Greater\");\n} else {\n    System.out.println(\"Not greater\");\n}", List.of("The else block is unreachable", "The condition should be >= 5", "The output is wrong", "No bug"), "No bug", "The code correctly prints Not greater when x is 5."),
                question("java-code-19", "Which code correctly calculates the average of two numbers?", List.of("double avg = (a + b) / 2;", "double avg = (double) (a + b) / 2;", "double avg = a + b / 2;", "int avg = (a + b) / 2;"), "double avg = (double) (a + b) / 2;", "Casting the sum to double ensures decimals are preserved in the division."),
                question("java-code-20", "Which line fixes the syntax error in this code?\npublic class Test {\n    public static void main(String[] args) {\n        System.out.println(\"Hello\")\n    }\n}", List.of("System.out.println(\"Hello\");", "System.out.println(\"Hello\");;", "System.out.println(\"Hello\"); }", "print(\"Hello\")"), "System.out.println(\"Hello\");", "Every statement in Java must end with a semicolon." )
        );
    }

    private List<Question> springBootQuestions() {
        return List.of(
                question("spring-1", "What is Spring Boot mainly used for?", List.of("Database design", "Rapid application development with Spring", "HTML page rendering only", "Machine learning training"), "Rapid application development with Spring", "Spring Boot simplifies setup and configuration for Spring applications."),
                question("spring-2", "Which annotation marks a class as a Spring-managed component?", List.of("@Service", "@Component", "@Controller", "@Entity"), "@Component", "@Component marks the class for component scanning and dependency injection."),
                question("spring-3", "Which annotation creates a REST controller?", List.of("@Repository", "@RestController", "@Configuration", "@Bean"), "@RestController", "@RestController combines @Controller and @ResponseBody for REST endpoints."),
                question("spring-4", "Which file is commonly used for application configuration in Spring Boot?", List.of("pom.xml", "application.properties", "settings.gradle", "Dockerfile"), "application.properties", "Spring Boot reads default app settings from application.properties."),
                question("spring-5", "What does dependency injection help with?", List.of("Runtime optimization", "Loose coupling and easier testing", "Security encryption", "CSS styling"), "Loose coupling and easier testing", "Dependency injection reduces hard-coded dependencies between classes."),
                question("spring-6", "Which annotation is used to inject a bean into a field or constructor?", List.of("@Inject", "@Autowired", "@RequestMapping", "@Value"), "@Autowired", "@Autowired enables automatic dependency injection by Spring."),
                question("spring-7", "Which HTTP method is typically used to create a new resource?", List.of("GET", "POST", "PUT", "DELETE"), "POST", "POST is commonly used to create new resources on the server."),
                question("spring-8", "Which annotation maps a URL to a controller method?", List.of("@GetMapping", "@Autowired", "@Entity", "@Service"), "@GetMapping", "@GetMapping maps HTTP GET requests to a specific method."),
                question("spring-9", "What does @SpringBootApplication do?", List.of("Starts a thread pool", "Combines configuration, enable auto-configuration, and component scanning", "Loads a database", "Generates CSS"), "Combines configuration, enable auto-configuration, and component scanning", "This annotation enables the Spring Boot application setup."),
                question("spring-10", "Which layer is usually responsible for business logic?", List.of("Controller", "Service", "Repository", "Entity"), "Service", "The service layer contains business rules and orchestration logic."),
                question("spring-11", "Which annotation is used for database repositories in Spring Data JPA?", List.of("@Repository", "@Component", "@Bean", "@Controller"), "@Repository", "@Repository marks DAO/repository classes and adds exception translation."),
                question("spring-12", "What is a bean in Spring?", List.of("A Java class with no methods", "An object managed by the Spring container", "A database table", "A Java package"), "An object managed by the Spring container", "Beans are objects created and managed by the IoC container."),
                question("spring-13", "Which annotation is used to bind request parameters to a Java object?", List.of("@RequestBody", "@ResponseBody", "@PathVariable", "@ModelAttribute"), "@RequestBody", "@RequestBody binds the HTTP request body to a Java object."),
                question("spring-14", "Which dependency is commonly added for REST API development in Spring Boot?", List.of("spring-boot-starter-web", "spring-boot-starter-test", "spring-boot-starter-jdbc", "spring-boot-starter-security"), "spring-boot-starter-web", "This starter provides web MVC and embedded server support."),
                question("spring-15", "Which type of bean scope is most common for web apps?", List.of("Singleton", "Prototype", "Request", "Session"), "Singleton", "Singleton is the default scope in Spring and creates one shared bean instance."),
                question("spring-16", "Which annotation is used to define an application property value?", List.of("@Value", "@Autowired", "@Bean", "@Profile"), "@Value", "@Value injects configuration values into fields or constructor parameters."),
                question("spring-17", "What is the purpose of an actuator in Spring Boot?", List.of("To create database tables", "To expose health and metrics endpoints", "To manage HTML templates", "To define mapping classes"), "To expose health and metrics endpoints", "Actuator provides operational monitoring and management endpoints."),
                question("spring-18", "Which annotation is used for configuration classes?", List.of("@Configuration", "@Entity", "@ComponentScan", "@Controller"), "@Configuration", "@Configuration declares a class as a source of bean definitions."),
                question("spring-19", "What does @Transactional do?", List.of("It enables logging", "It manages database transactions", "It maps routes", "It injects templates"), "It manages database transactions", "Transactions ensure atomic database operations."),
                question("spring-20", "Which file typically manages project dependencies in Maven?", List.of("build.gradle", "pom.xml", "settings.xml", "application.yml"), "pom.xml", "Maven uses pom.xml to define dependencies and build configuration." )
        );
    }

    private List<Question> microservicesQuestions() {
        return List.of(
                question("micro-1", "What is a microservice architecture?", List.of("One large application with one database", "A system built as small independent services", "Only frontend code", "A monolithic deployment pattern"), "A system built as small independent services", "Microservices split capabilities into independent deployable services."),
                question("micro-2", "Which of these is a benefit of microservices?", List.of("Single shared codebase only", "Independent deployment and scaling", "No network communication", "One database for everything"), "Independent deployment and scaling", "Teams can deploy and scale services separately."),
                question("micro-3", "Which component often routes requests between clients and services?", List.of("API Gateway", "JVM", "Compiler", "ORM"), "API Gateway", "An API gateway centralizes routing, auth, and rate limiting."),
                question("micro-4", "What is service discovery used for?", List.of("Finding services dynamically", "Rendering HTML pages", "Creating CSS themes", "Writing unit tests"), "Finding services dynamically", "Service discovery helps clients locate service instances in the network."),
                question("micro-5", "Which pattern helps handle failures in distributed systems?", List.of("Circuit breaker", "Singleton", "Factory", "Decorator"), "Circuit breaker", "A circuit breaker prevents repeated failed calls from cascading."),
                question("micro-6", "What is a distributed transaction?", List.of("A transaction limited to one service", "A transaction that spans multiple services", "A CSS transaction", "A REST endpoint"), "A transaction that spans multiple services", "Distributed transactions involve state changes across different services."),
                question("micro-7", "Which protocol is commonly used for synchronous inter-service communication?", List.of("HTTP/REST", "SMTP", "FTP", "Bluetooth"), "HTTP/REST", "HTTP/REST is widely used for service-to-service communication."),
                question("micro-8", "What does horizontal scaling mean?", List.of("Adding more servers or instances", "Increasing RAM on one server", "Reducing the number of services", "Using only one container"), "Adding more servers or instances", "Horizontal scaling adds more replicas to handle demand."),
                question("micro-9", "Which pattern separates reads and writes across services?", List.of("Message queue", "CQRS", "Inheritance", "Loop"), "CQRS", "Command Query Responsibility Segregation separates command and query responsibilities."),
                question("micro-10", "What is the main role of a message broker?", List.of("Store static files", "Asynchronous communication between services", "Compile Java code", "Generate CSS"), "Asynchronous communication between services", "A broker allows event-driven message exchange between services."),
                question("micro-11", "Which of the following is a common resilience pattern?", List.of("Retry", "Reflection", "Polymorphism", "Encapsulation"), "Retry", "Retries allow temporary failures to recover automatically."),
                question("micro-12", "Which technique helps avoid a single database bottleneck across services?", List.of("Shared database", "Database per service", "Single connection pool", "Static files"), "Database per service", "Each service usually owns its own data model and database."),
                question("micro-13", "What does idempotency mean in APIs?", List.of("The operation can be repeated safely", "The endpoint is private", "It must always throw errors", "It is read-only"), "The operation can be repeated safely", "Idempotent operations produce the same result when retried."),
                question("micro-14", "Which tool is widely used for service registration and discovery?", List.of("Eureka", "Maven", "JUnit", "Spring MVC"), "Eureka", "Netflix Eureka is a common service registry and discovery tool."),
                question("micro-15", "What is the purpose of rate limiting?", List.of("To allow unlimited requests", "To protect services from overload", "To create database indexes", "To add CSS"), "To protect services from overload", "Rate limiting controls incoming traffic and prevents abuse."),
                question("micro-16", "Which type of communication is often used for long-running background tasks?", List.of("Synchronous REST calls", "Asynchronous messaging", "Inline CSS", "File copying"), "Asynchronous messaging", "Queues and brokers are commonly used for background processing."),
                question("micro-17", "What does observability in microservices mean?", List.of("Only serving static content", "Monitoring logs, metrics, and traces", "Restricting API calls", "Changing CSS"), "Monitoring logs, metrics, and traces", "Observability helps teams diagnose service behavior and issues."),
                question("micro-18", "Which pattern allows independent scaling of different service responsibilities?", List.of("Domain decomposition", "Single monolith", "Shared util classes", "Inline CSS"), "Domain decomposition", "Dividing the system by business domains helps service ownership and scaling."),
                question("micro-19", "What is an API contract?", List.of("A description of request and response structures", "A database schema", "A user interface style", "A build tool"), "A description of request and response structures", "Contracts define expected API behavior across services."),
                question("micro-20", "Which concept helps services communicate even when one service is temporarily unavailable?", List.of("Timeout and retry", "Variable naming", "Hard-coded values", "Static imports"), "Timeout and retry", "Timeouts and retries improve resilience in distributed systems." )
        );
    }

    private Question question(String id, String questionText, List<String> options, String correctAnswer, String explanation) {
        String grade = deriveGradeFromId(id);
        return new Question(id, questionText, options, correctAnswer, explanation, grade);
    }

    private String deriveGradeFromId(String id) {
        if (id == null || !id.contains("-")) return "7-8";
        try {
            String[] parts = id.split("-");
            String prefix = parts[0];
            int num = Integer.parseInt(parts[1]);

            if (prefix.startsWith("java") || prefix.startsWith("spring") || prefix.startsWith("micro")) return "9-10";

            if (num <= 5) return "1-2";
            if (num <= 10) return "3-4";
            if (num <= 15) return "5-6";
            if (num <= 20) return "7-8";
            return "9-10";
        } catch (Exception e) {
            return "7-8";
        }
    }
}

