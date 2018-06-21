import PingPongScoresStrategy from './PingPongScoresStrategy.js';
import TennisScoresStrategy from './TennisScoresStrategy.js';

const possibleScoresStrategies = {
    TE: new TennisScoresStrategy(),
    PP: new PingPongScoresStrategy()
};

export default possibleScoresStrategies;
