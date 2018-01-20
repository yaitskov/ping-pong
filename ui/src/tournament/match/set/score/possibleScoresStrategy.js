import PingPongScoresStrategy from './PingPongScoresStrategy.js';
import TennisScoresStrategy from './TennisScoresStrategy.js';

const possibleScoresStrategies = {
    Tennis: new TennisScoresStrategy(),
    PingPong: new PingPongScoresStrategy()
};

export default possibleScoresStrategies;
