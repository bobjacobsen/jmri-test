(function($) {
    $.extend({
        JMRI: function(url, bindings) {
            var jmri = new Object();
            if (typeof (url) === 'string') {
                jmri.url = url;
            } else {
                jmri.url = document.URL.split('/')[0] + "//" + document.URL.split('/')[2] + "/json/";
                bindings = url;
            }
            // Default event handlers that do nothing
            jmri.console = function(data) {
            };
            jmri.error = function(error) {
            };
            jmri.light = function(name, state, data) {
            };
            jmri.memory = function(name, value, data) {
            };
            jmri.power = function(state) {
            };
            jmri.railroad = function(string) {
            };
            jmri.reporter = function(name, value, data) {
            };
            jmri.rosterEntry = function(id, data) {
            };
            jmri.route = function(name, state, data) {
            };
            jmri.sensor = function(name, state, data) {
            };
            jmri.signalHead = function(name, state, data) {
            };
            jmri.signalMast = function(name, state, data) {
            };
            jmri.throttle = function(id, data) {
            };
            jmri.time = function(time, data) {
            };
            jmri.turnout = function(name, state, data) {
            };
            jmri.version = function(string) {
            };
            // Add user-defined handlers to the settings object
            $.extend(jmri, bindings);
            // Constants
            jmri.UNKNOWN = 0;
            jmri.POWER_ON = 2;
            jmri.POWER_OFF = 4;
            // Getters and Setters
            jmri.getLight = function(name) {
                if (jmri.socket) {
                    jmri.socket.send("light", {name: name, state: jmri.UNKNOWN});
                } else {
                    $.getJSON(jmri.url + "light/" + name, function(json) {
                        jmri.light(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setLight = function(name, state) {
                if (jmri.socket) {
                    jmri.socket.send("light", {name: name, state: state});
                } else {
                    $.post(jmri.url + "light/" + name + "?state=" + state, JSON.stringify({state: state}), function(json) {
                        setTimeout("jmri.getLight(\"" + json.data.name + "\")", 1000);
                    });
                }
            };
            jmri.getMemory = function(name) {
                if (jmri.socket) {
                    jmri.socket.send("memory", {name: name});
                } else {
                    $.getJSON(jmri.url + "memory/" + name, function(json) {
                        jmri.memory(json.data.name, json.data.value, json.data);
                    });
                }
            };
            jmri.getObject = function(type, name) {
                switch (type) {
                    case "light":
                        jmri.getLight(name);
                        break;
                    case "memory":
                        jmri.getMemory(name);
                        break;
                    case "rosterEntry":
                        jmri.getRosterEntry(name);
                        break;
                    case "route":
                        jmri.getRoute(name);
                        break;
                    case "sensor":
                        jmri.getSensor(name);
                        break;
                    case "signalHead":
                        jmri.getSignalHead(name);
                        break;
                    case "signalMast":
                        jmri.getSignalMast(name);
                        break;
                    case "turnout":
                        jmri.getTurnout(name);
                        break;
                }
            };
            jmri.setObject = function(type, name, state) {
                switch (type) {
                    case "light":
                        jmri.setLight(name, state);
                        break;
                    case "memory":
                        jmri.setMemory(name, state);
                        break;
                    case "rosterEntry":
                        jmri.setRosterEntry(name, state);
                        break;
                    case "route":
                        jmri.setRoute(name, state);
                        break;
                    case "sensor":
                        jmri.setSensor(name, state);
                        break;
                    case "signalHead":
                        jmri.setSignalHead(name, state);
                        break;
                    case "signalMast":
                        jmri.setSignalMast(name, state);
                        break;
                    case "turnout":
                        jmri.setTurnout(name, state);
                        break;
                }
            };
            jmri.getPower = function() {
                if (jmri.socket) {
                    jmri.socket.send("power", {state: jmri.UNKNOWN});
                } else {
                    $.getJSON(jmri.url + "power", function(json) {
                        jmri.power(json.data.state);
                    });
                }
            };
            jmri.setPower = function(state) {
                if (jmri.socket) {
                    jmri.socket.send("power", {state: state});
                } else {
                    $.post(jmri.url + "power?state=" + state, JSON.stringify({state: state}), function(json) {
                        setTimeout("jmri.getPower()", 1000);
                    });
                }
            };
            jmri.getRosterEntry = function(id) {
                if (jmri.socket) {
                    jmri.socket.send("rosterEntry", {name: id});
                } else {
                    $.getJSON(jmri.url + "rosterEntry/" + id, function(json) {
                        jmri.rosterEntry(json.data.name, json.data);
                    });
                }
            };
            jmri.getRoute = function(name) {
                if (jmri.socket) {
                    jmri.socket.send("route", {name: name, state: UNKNOWN});
                } else {
                    $.getJSON(jmri.url + "route/" + name, function(json) {
                        jmri.signalHead(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setRoute = function(name, state) {
                if (jmri.socket) {
                    jmri.socket.send("route", {name: name, state: state});
                } else {
                    $.post(jmri.url + "route/" + name + "?state=" + state, JSON.stringify({state: state}), function(json) {
                        setTimeout("jmri.getRoute(\"" + json.data.name + "\")", 1000);
                    });
                }
            };
            jmri.getSensor = function(name) {
                if (jmri.socket) {
                    jmri.socket.send("sensor", {name: name, state: UNKNOWN});
                } else {
                    $.getJSON(jmri.url + "sensor/" + name, function(json) {
                        jmri.signalHead(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setSensor = function(name, state) {
                if (jmri.socket) {
                    jmri.socket.send("sensor", {name: name, state: state});
                } else {
                    $.post(jmri.url + "sensor/" + name + "?state=" + state, JSON.stringify({state: state}), function(json) {
                        setTimeout("jmri.getSensor(\"" + json.data.name + "\")", 1000);
                    });
                }
            };
            jmri.getSignalHead = function(name) {
                if (jmri.socket) {
                    jmri.socket.send("signalHead", {name: name, state: UNKNOWN});
                } else {
                    $.getJSON(jmri.url + "signalHead/" + name, function(json) {
                        jmri.signalHead(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setSignalHead = function(name, state) {
                if (jmri.socket) {
                    jmri.socket.send("signalHead", {name: name, state: state});
                } else {
                    $.post(jmri.url + "signalHead/" + name + "?state=" + state, JSON.stringify({state: state}), function(json) {
                        setTimeout("jmri.getSignalHead(\"" + json.data.name + "\")", 1000);
                    });
                }
            };
            jmri.getSignalMast = function(name) {
                if (jmri.socket) {
                    jmri.socket.send("signalMast", {name: name, state: UNKNOWN});
                } else {
                    $.getJSON(jmri.url + "signalMast/" + name, function(json) {
                        jmri.signalHead(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setSignalMast = function(name, state) {
                if (jmri.socket) {
                    jmri.socket.send("signalMast", {name: name, state: state});
                } else {
                    $.post(jmri.url + "signalMast/" + name + "?state=" + state, JSON.stringify({state: state}), function(json) {
                        setTimeout("jmri.getSignalMast(\"" + json.data.name + "\")", 1000);
                    });
                }
            };
            jmri.getTime = function() {
                if (jmri.socket) {
                    jmri.socket.send("time", {});
                } else {
                    $.getJSON(jmri.url + "time", function(json) {
                        jmri.time(json.data.time, json.data);
                    });
                }
            };
            jmri.getTurnout = function(name) {
                if (jmri.socket) {
                    jmri.socket.send("turnout", {name: name, state: jmri.UNKNOWN});
                } else {
                    $.getJSON(jmri.url + "turnout/" + name, function(json) {
                        jmri.turnout(json.data.name, json.data.state, json.data);
                    });
                }
            };
            jmri.setTurnout = function(name, state) {
                if (jmri.socket) {
                    jmri.socket.send("turnout", {name: name, state: state});
                } else {
                    $.post(jmri.url + "turnout/" + name + "?state=" + state, JSON.stringify({state: state}), function(json) {
                        setTimeout("jmri.getTurnout(\"" + json.data.name + "\")", 1000);
                    });
                }
            };
            // Heartbeat
            jmri.heartbeat = function() {
                jmri.socket.send("ping");
            };
            jmri.heartbeatInterval = null;
            // WebSocket
            jmri.socket = $.websocket(jmri.url.replace(/^http/, "ws"), {
                // stop the heartbeat when the socket closes
                close: function() {
                    clearInterval(jmri.heartbeat);
                },
                message: function(e) {
                    jmri.console(e.originalEvent.data);
                },
                events: {
                    // TODO: add constist, programmer, and operations events
                    error: function(e) {
                        jmri.error(e.data);
                    },
                    goodbye: function(e) {
                        jmri.socket.close();
                    },
                    // handle the initial handshake response from the server
                    hello: function(e) {
                        jmri.heartbeatInterval = setInterval(jmri.heartbeat, e.data.heartbeat);
                        jmri.version(e.data.JMRI);
                        jmri.railroad(e.data.railroad);
                    },
                    light: function(e) {
                        jmri.light(e.data.name, e.data.state, e.data);
                    },
                    memory: function(e) {
                        jmri.memory(e.data.name, e.data.value, e.data);
                    },
                    power: function(e) {
                        jmri.power(e.data.state);
                    },
                    reporter: function(e) {
                        jmri.reporter(e.data.name, e.data.value, e.data);
                    },
                    route: function(e) {
                        jmri.route(e.data.name, e.data.state, e.data);
                    },
                    sensor: function(e) {
                        jmri.sensor(e.data.name, e.data.state, e.data);
                    },
                    signalHead: function(e) {
                        jmri.signalHead(e.data.name, e.data.state, e.data);
                    },
                    signalMast: function(e) {
                        jmri.signalMast(e.data.name, e.data.state, e.data);
                    },
                    throttle: function(e) {
                        jmri.throttle(e.data.throttle, e.data);
                    },
                    time: function(e) {
                        jmri.time(e.data.time, e.data);
                    },
                    turnout: function(e) {
                        jmri.turnout(e.data.name, e.data.state, e.data);
                    }
                }
            });
            if (jmri.socket === null) {
                $("#no-websockets").addClass("show").removeClass("hidden");
            }
            return jmri;
        }
    });
})(jQuery);
