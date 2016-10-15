/**
 * Copyright (c) 2014, 2016, jMonkeyEngine. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of 'jMonkeyEngine' nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.ai.agents.behaviors.npc.steering;

import com.jme3.ai.agents.AIControl;
import com.jme3.ai.agents.Agent;
import com.jme3.ai.agents.behaviors.Behavior;
import com.jme3.ai.agents.behaviors.npc.steering.SteeringExceptions.IllegalIntervalException;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.util.LinkedList;
import java.util.List;

/**
 * Base class for all steering behaviors. This behavior contains some
 * attributes that are all common for steering behaviors. <br><br>
 *
 * You can change the braking factor (to a value lower than 1) so that this
 * behavior will slow down the agent velocity.
 *
 * @author Tihomir Radosavljević
 * @author Jesús Martín Berlanga
 * @author MeFisto94
 * @version 1.5.3
 */
public abstract class AbstractSteeringBehavior extends Behavior {

    /**
     * Brake intesity of steering behavior.
     */
    private float brakingFactor = 1;
    /**
     * Velocity of our agent.
     */
    protected Vector3f velocity;
    /**
     * Time per frame.
     */
    protected float timePerFrame;

    /**
     * Instantiate a new Behavior. Agent is passed when you add this behavior to
     * an agent.
     */
    public AbstractSteeringBehavior() {
        super();
        velocity = new Vector3f();
    }

    /**
     * Method for calculating steering vector.
     *
     * @return
     */
    protected abstract Vector3f calculateSteering();

    /**
     * Method for calculating new velocity of agent based on steering vector.
     *
     * @param tpf The time that has passed since the call
     * @see AbstractSteeringBehavior#calculateSteering()
     * @return The new velocity for this agent based on steering vector
     */
    protected Vector3f calculateNewVelocity(float tpf) {
        velocity.addLocal(
                calculateSteering().mult(1 / agent.getMass()).multLocal(tpf)
        );

        if (velocity.lengthSquared() >
           (agent.getMaxMoveSpeed() * agent.getMaxMoveSpeed())) {
            velocity = velocity.normalize().mult(agent.getMaxMoveSpeed());
        }
        
        agent.setVelocity(velocity);
        return velocity;
    }

    /**
     * Method for rotating agent in direction of velocity of agent.
     *
     * @param tpf time per frame
     */
    protected void rotateAgent(float tpf) {
        Quaternion q = new Quaternion();
        q.lookAt(velocity, new Vector3f(0, 1, 0));
        agent.getSpatial().getLocalRotation().slerp(q, agent.getRotationSpeed() * tpf);
    }

    /**
     *
     * @return current velocity of agent.
     */
    public Vector3f getVelocity() {
        return velocity;
    }

    /**
     * Setting current velocity of agent.
     *
     * @param velocity
     */
    public void setVelocity(Vector3f velocity) {
        this.velocity = velocity;
    }

    /**
     * Note that 0 means the maximum braking force and 1 No braking force
     *
     * @throws IllegalIntervalException If the braking factor is not contained
     * in the [0,1] interval
     */
    protected final void setBrakingFactor(float brakingFactor) {
        if (brakingFactor < 0 || brakingFactor > 1) {
            throw new IllegalIntervalException("braking", brakingFactor);
        }
        this.brakingFactor = brakingFactor;
    }

    public final float getBrakingFactor() {
        return this.brakingFactor;
    }

    /**
     * Usual update pattern for steering behaviors. <br><br>
     *
     * The final velocity is multiplied by the braking factor.
     *
     * @param tpf
     */
    @Override
    public void updateAI(float tpf) {
        this.timePerFrame = tpf;
        
        // Calculate Velocity
        calculateNewVelocity(tpf);
        
        // Apply Braking Force
        velocity.multLocal(this.brakingFactor);
        
        switch (agent.getApplyType())
        {
            case Spatial:
                agent.setWorldTranslation(agent.getPredictedPosition(tpf));
                rotateAgent(tpf);
                break;
                
            case BetterCharacterControl:
            case RigidBody:
                throw new RuntimeException("The ApplyType " +
                        agent.getApplyType() + " isn't supported yet, sorry.");
                
            case DontApply:
                break;
        }
    }

    public float getTimePerFrame() {
        return timePerFrame;
    }

    public void setTimePerFrame(float timePerFrame) {
        this.timePerFrame = timePerFrame;
    }

    /**
     * Convenience method for converting list of agents to list of entities.
     *
     * @param agents
     * @return
     */
    protected List<AIControl> convertToGameEntities(List<Agent> agents) {
        List<AIControl> entities = new LinkedList<AIControl>();
        for (Agent tempAgent : agents) {
            entities.add(tempAgent);
        }
        return entities;
    }
}
