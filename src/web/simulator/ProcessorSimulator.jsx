import React from 'react';
import { Col, Input, Row } from 'react-bootstrap';
import naturalSort from 'javascript-natural-sort';

import { Select } from 'components/common';
import RawMessageLoader from 'components/messageloaders/RawMessageLoader';
import SimulationResults from './SimulationResults';

import SimulatorActions from './SimulatorActions';
// eslint-disable-next-line no-unused-vars
import SimulatorStore from './SimulatorStore';

const ProcessorSimulator = React.createClass({
  propTypes: {
    streams: React.PropTypes.array.isRequired,
  },

  getInitialState() {
    return {
      message: undefined,
      stream: this.props.streams.find(s => s.id.toLowerCase() === 'default'),
      simulation: undefined,
      loading: false,
      error: undefined,
    };
  },

  _onMessageLoad(message, options) {
    this.setState({ message: message, simulation: undefined, loading: true, error: undefined });

    SimulatorActions.simulate
      .triggerPromise(this.state.stream, message.fields, options.inputId)
      .then(
        response => {
          this.setState({ simulation: response, loading: false });
        },
        error => {
          this.setState({ loading: false, error: error });
        }
      );
  },

  _getFormattedStreams(streams) {
    if (!streams) {
      return [];
    }

    return streams
      .map(s => {
        return { value: s.id, label: s.title };
      })
      .sort((s1, s2) => naturalSort(s1.label, s2.label));
  },

  _onStreamSelect(selectedStream) {
    const stream = this.props.streams.find(s => s.id.toLowerCase() === selectedStream.toLowerCase());
    this.setState({ stream: stream });
  },

  render() {
    const streamHelp = (
      <span>
        Select a stream to use during simulation, the <em>Default</em> stream is used by default.
      </span>
    );

    return (
      <div>
        <Row>
          <Col md={12}>
            <h1>Load a message</h1>
            <p>
              Build an example message that will be used in the simulation.{' '}
              <strong>No real messages stored in Graylog will be changed. All actions are purely simulated on the
                temporary input you provide below.</strong>
            </p>
            <Row className="row-sm">
              <Col md={7}>
                <Input label="Stream"
                       help={streamHelp}>
                  <Select options={this._getFormattedStreams(this.props.streams)}
                          onValueChange={this._onStreamSelect} value={this.state.stream.id} clearable={false} />
                </Input>
              </Col>
            </Row>
            <RawMessageLoader onMessageLoaded={this._onMessageLoad} inputIdSelector />
          </Col>
        </Row>
        <SimulationResults stream={this.state.stream}
                           originalMessage={this.state.message}
                           simulationResults={this.state.simulation}
                           isLoading={this.state.loading}
                           error={this.state.error} />
      </div>
    );
  },
});

export default ProcessorSimulator;
